package com.jhict.quality.engine;

import com.jhict.quality.engine.model.JudgmentInput;
import com.jhict.quality.engine.model.JudgmentOutput;
import com.jhict.quality.dto.StandardCandidateQuery;
import com.jhict.quality.enums.JudgmentType;
import com.jhict.quality.service.api.StandardService;
import com.jhict.quality.vo.QcQualityStandardDetailVO;
import com.jhict.quality.vo.StandardCandidateSetVO;
import com.jhict.quality.vo.StandardCandidateVO;
import com.jhict.quality.vo.StandardConflictDraftVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 质量判定引擎（核心）
 * 三级优先级标准匹配：客户协议 > 企标 > 国标（MySQL 5.7兼容）
 */
@Slf4j
@Component
public class JudgmentEngine {

    @Resource
    private StandardService standardService;

    /**
     * 执行质量判定
     *
     * @param input 判定输入
     * @return 判定结果
     */
    public JudgmentOutput judge(JudgmentInput input) {
        log.info("开始执行质量判定，recordId={}, variety={}, grade={}",
                input.getRecordId(), input.getProductVariety(), input.getProductGrade());

        LocalDate testDate = input.getTestTime().toLocalDate();
        String variety = input.getProductVariety();
        String grade = input.getProductGrade();
        String customerId = input.getCustomerId();

        List<String> allIndicatorIds = input.getValues().stream()
                .map(JudgmentInput.InspectionValueItem::getIndicatorId)
                .distinct()
                .collect(Collectors.toList());

        // Step 1: 查询候选标准集，先处理同优先级阻断冲突。
        StandardCandidateQuery candidateQuery = new StandardCandidateQuery();
        candidateQuery.setCustomerId(customerId);
        candidateQuery.setVariety(variety);
        candidateQuery.setGrade(grade);
        candidateQuery.setProductSpec(input.getProductSpec());
        candidateQuery.setTestDate(testDate);
        candidateQuery.setIndicatorIds(allIndicatorIds);
        StandardCandidateSetVO candidateSet = standardService.findCandidateStandards(candidateQuery);
        List<JudgmentOutput.StandardConflictItem> conflictItems = toOutputConflicts(candidateSet.getConflicts());
        boolean hasBlockingConflict = conflictItems.stream()
                .anyMatch(conflict -> "BLOCKING".equals(conflict.getConflictLevel()));

        if (hasBlockingConflict) {
            log.warn("发现阻断标准冲突，recordId={}, variety={}, grade={}", input.getRecordId(), variety, grade);
            return JudgmentOutput.builder()
                    .judgmentType(JudgmentType.STANDARD_CONFLICT)
                    .evidences(new ArrayList<>())
                    .gaps(new ArrayList<>())
                    .matchedStandardIds(collectConflictStandardIds(conflictItems))
                    .conflicts(conflictItems)
                    .build();
        }

        StandardCandidateVO matchedStandard = candidateSet.getSelectedStandard();

        List<String> matchedStandardIds = new ArrayList<>();
        List<JudgmentOutput.EvidenceItem> evidences = new ArrayList<>();
        List<JudgmentOutput.StandardGapItem> gaps = new ArrayList<>();

        if (matchedStandard == null) {
            // 无任何标准覆盖，所有指标均作为缺口
            log.warn("未找到适用标准，variety={}, grade={}, testDate={}", variety, grade, testDate);
            for (JudgmentInput.InspectionValueItem valueItem : input.getValues()) {
                gaps.add(JudgmentOutput.StandardGapItem.builder()
                        .indicatorId(valueItem.getIndicatorId())
                        .variety(variety)
                        .grade(grade)
                        .build());
            }
            return JudgmentOutput.builder()
                    .judgmentType(JudgmentType.UNQUALIFIED)
                    .evidences(evidences)
                    .gaps(gaps)
                    .matchedStandardIds(matchedStandardIds)
                    .conflicts(conflictItems)
                    .build();
        }

        return judgeAgainstStandard(input, matchedStandard.getId(), conflictItems);
    }

    /**
     * 按人工裁决后的控制标准重新执行规则判定。
     *
     * @param input      判定输入
     * @param standardId 裁决控制标准ID
     * @return 判定结果
     */
    public JudgmentOutput judgeWithStandard(JudgmentInput input, String standardId) {
        log.info("按指定标准重新执行质量判定，recordId={}, standardId={}", input.getRecordId(), standardId);
        return judgeAgainstStandard(input, standardId, new ArrayList<>());
    }

    private JudgmentOutput judgeAgainstStandard(JudgmentInput input,
                                                String standardId,
                                                List<JudgmentOutput.StandardConflictItem> conflictItems) {
        String variety = input.getProductVariety();
        String grade = input.getProductGrade();
        List<String> matchedStandardIds = new ArrayList<>();
        List<JudgmentOutput.EvidenceItem> evidences = new ArrayList<>();
        List<JudgmentOutput.StandardGapItem> gaps = new ArrayList<>();
        matchedStandardIds.add(standardId);

        // 查询该标准下所有指标配置，建立 indicatorId -> StandardIndicatorDetail 映射
        QcQualityStandardDetailVO standardDetail = standardService.getById(standardId);
        Map<String, QcQualityStandardDetailVO.StandardIndicatorDetail> siMap = standardDetail.getIndicators().stream()
                .collect(Collectors.toMap(QcQualityStandardDetailVO.StandardIndicatorDetail::getIndicatorId, si -> si));

        // 逐指标比较，计算偏差，收集evidence和gap
        // 四段优先级：UNQUALIFIED > NEED_REINSPECTION > CAN_CONCESSION > QUALIFIED（D-015）
        boolean hasUnqualified = false;
        boolean hasReinspection = false;
        boolean hasConcession = false;

        for (JudgmentInput.InspectionValueItem valueItem : input.getValues()) {
            String indicatorId = valueItem.getIndicatorId();
            BigDecimal testValue = valueItem.getTestValue();

            QcQualityStandardDetailVO.StandardIndicatorDetail si = siMap.get(indicatorId);
            if (si == null) {
                // 该指标在标准中无配置 -> 记为缺口
                gaps.add(JudgmentOutput.StandardGapItem.builder()
                        .indicatorId(indicatorId)
                        .variety(variety)
                        .grade(grade)
                        .build());
                log.debug("指标 {} 在标准 {} 中无配置，记录为缺口", indicatorId, standardId);
                continue;
            }

            // 文本型指标（testValue为null），仅记录为通过（文本型无法数值比较）
            if (testValue == null) {
                evidences.add(JudgmentOutput.EvidenceItem.builder()
                        .standardId(standardId)
                        .indicatorId(indicatorId)
                        .testValue(null)
                        .upperLimit(si.getUpperLimit())
                        .lowerLimit(si.getLowerLimit())
                        .deviation(BigDecimal.ZERO)
                        .triggerRule("文本型指标，不参与数值判定")
                        .passed(true)
                        .build());
                continue;
            }

            BigDecimal upperLimit = si.getUpperLimit();
            BigDecimal lowerLimit = si.getLowerLimit();
            BigDecimal concessionUpper = si.getConcessionUpper();
            BigDecimal concessionLower = si.getConcessionLower();

            // 判断是否在正常范围内
            boolean withinNormal = isWithinRange(testValue, lowerLimit, upperLimit);

            if (withinNormal) {
                // 正常通过
                evidences.add(JudgmentOutput.EvidenceItem.builder()
                        .standardId(standardId)
                        .indicatorId(indicatorId)
                        .testValue(testValue)
                        .upperLimit(upperLimit)
                        .lowerLimit(lowerLimit)
                        .deviation(BigDecimal.ZERO)
                        .triggerRule(buildPassedRule(lowerLimit, upperLimit))
                        .passed(true)
                        .build());
            } else {
                // 超出正常范围，按四段优先级判断（D-015 / FR-005）
                BigDecimal deviation = calcDeviation(testValue, lowerLimit, upperLimit);
                boolean lowerViolation = lowerLimit != null && testValue.compareTo(lowerLimit) < 0;
                boolean upperViolation = upperLimit != null && testValue.compareTo(upperLimit) > 0;
                // 仅在违规方向配置了让步限值时，才视为「已配置让步范围」
                boolean hasConcessionOnViolatedSide = (lowerViolation && concessionLower != null)
                        || (upperViolation && concessionUpper != null);
                boolean withinConcession = false;
                if (lowerViolation && concessionLower != null) {
                    withinConcession = testValue.compareTo(concessionLower) >= 0
                            && (concessionUpper == null || testValue.compareTo(concessionUpper) <= 0);
                } else if (upperViolation && concessionUpper != null) {
                    withinConcession = (concessionLower == null || testValue.compareTo(concessionLower) >= 0)
                            && testValue.compareTo(concessionUpper) <= 0;
                }

                if (withinConcession) {
                    // ② 在让步范围内 → CAN_CONCESSION
                    hasConcession = true;
                    evidences.add(JudgmentOutput.EvidenceItem.builder()
                            .standardId(standardId)
                            .indicatorId(indicatorId)
                            .testValue(testValue)
                            .upperLimit(upperLimit)
                            .lowerLimit(lowerLimit)
                            .deviation(deviation)
                            .triggerRule(buildConcessionRule(lowerLimit, upperLimit, concessionLower, concessionUpper))
                            .passed(false)
                            .build());
                } else if (!hasConcessionOnViolatedSide) {
                    // ③ 超出合格限且违规方向未配置让步范围 → NEED_REINSPECTION
                    hasReinspection = true;
                    evidences.add(JudgmentOutput.EvidenceItem.builder()
                            .standardId(standardId)
                            .indicatorId(indicatorId)
                            .testValue(testValue)
                            .upperLimit(upperLimit)
                            .lowerLimit(lowerLimit)
                            .deviation(deviation)
                            .triggerRule(buildReinspectionRule(lowerLimit, upperLimit, testValue, deviation))
                            .passed(false)
                            .build());
                } else {
                    // ④ 超出合格限且超出让步范围 → UNQUALIFIED
                    hasUnqualified = true;
                    evidences.add(JudgmentOutput.EvidenceItem.builder()
                            .standardId(standardId)
                            .indicatorId(indicatorId)
                            .testValue(testValue)
                            .upperLimit(upperLimit)
                            .lowerLimit(lowerLimit)
                            .deviation(deviation)
                            .triggerRule(buildUnqualifiedRule(lowerLimit, upperLimit, testValue, deviation))
                            .passed(false)
                            .build());
                }
            }
        }

        // Step 5: 汇总结论（四段优先级：UNQUALIFIED > NEED_REINSPECTION > CAN_CONCESSION > QUALIFIED）
        JudgmentType judgmentType;
        if (hasUnqualified) {
            judgmentType = JudgmentType.UNQUALIFIED;
        } else if (hasReinspection) {
            judgmentType = JudgmentType.NEED_REINSPECTION;
        } else if (hasConcession) {
            judgmentType = JudgmentType.CAN_CONCESSION;
        } else {
            judgmentType = JudgmentType.QUALIFIED;
        }

        log.info("质量判定完成，recordId={}, judgmentType={}, evidences={}, gaps={}",
                input.getRecordId(), judgmentType, evidences.size(), gaps.size());

        return JudgmentOutput.builder()
                .judgmentType(judgmentType)
                .evidences(evidences)
                .gaps(gaps)
                .matchedStandardIds(matchedStandardIds)
                .conflicts(conflictItems)
                .build();
    }

    /**
     * 判断实测值是否在[lowerLimit, upperLimit]范围内
     * null表示无约束（开区间）
     */
    private boolean isWithinRange(BigDecimal testValue, BigDecimal lowerLimit, BigDecimal upperLimit) {
        if (testValue == null) {
            return true;
        }
        if (lowerLimit != null && testValue.compareTo(lowerLimit) < 0) {
            return false;
        }
        if (upperLimit != null && testValue.compareTo(upperLimit) > 0) {
            return false;
        }
        return true;
    }

    /**
     * 计算偏差值（实测值超出限值的距离）
     */
    private BigDecimal calcDeviation(BigDecimal testValue, BigDecimal lowerLimit, BigDecimal upperLimit) {
        if (testValue == null) {
            return BigDecimal.ZERO;
        }
        if (upperLimit != null && testValue.compareTo(upperLimit) > 0) {
            return testValue.subtract(upperLimit).setScale(6, RoundingMode.HALF_UP);
        }
        if (lowerLimit != null && testValue.compareTo(lowerLimit) < 0) {
            return testValue.subtract(lowerLimit).setScale(6, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }

    private String buildPassedRule(BigDecimal lowerLimit, BigDecimal upperLimit) {
        StringBuilder sb = new StringBuilder("实测值在标准范围内");
        if (lowerLimit != null) {
            sb.append("，下限=").append(lowerLimit.stripTrailingZeros().toPlainString());
        }
        if (upperLimit != null) {
            sb.append("，上限=").append(upperLimit.stripTrailingZeros().toPlainString());
        }
        return sb.toString();
    }

    private String buildConcessionRule(BigDecimal lowerLimit, BigDecimal upperLimit,
                                       BigDecimal concessionLower, BigDecimal concessionUpper) {
        StringBuilder sb = new StringBuilder("实测值超出标准范围但在让步范围内");
        if (lowerLimit != null) {
            sb.append("，标准下限=").append(lowerLimit.stripTrailingZeros().toPlainString());
        }
        if (upperLimit != null) {
            sb.append("，标准上限=").append(upperLimit.stripTrailingZeros().toPlainString());
        }
        if (concessionLower != null) {
            sb.append("，让步下限=").append(concessionLower.stripTrailingZeros().toPlainString());
        }
        if (concessionUpper != null) {
            sb.append("，让步上限=").append(concessionUpper.stripTrailingZeros().toPlainString());
        }
        return sb.toString();
    }

    private String buildUnqualifiedRule(BigDecimal lowerLimit, BigDecimal upperLimit,
                                        BigDecimal testValue, BigDecimal deviation) {
        StringBuilder sb = new StringBuilder("实测值超出标准范围且超出让步范围");
        if (upperLimit != null && testValue != null && testValue.compareTo(upperLimit) > 0) {
            sb.append("，超上限").append(deviation.abs().stripTrailingZeros().toPlainString());
        } else if (lowerLimit != null && testValue != null && testValue.compareTo(lowerLimit) < 0) {
            sb.append("，低于下限").append(deviation.abs().stripTrailingZeros().toPlainString());
        }
        if (lowerLimit != null) {
            sb.append("，下限=").append(lowerLimit.stripTrailingZeros().toPlainString());
        }
        if (upperLimit != null) {
            sb.append("，上限=").append(upperLimit.stripTrailingZeros().toPlainString());
        }
        return sb.toString();
    }

    private String buildReinspectionRule(BigDecimal lowerLimit, BigDecimal upperLimit,
                                         BigDecimal testValue, BigDecimal deviation) {
        StringBuilder sb = new StringBuilder("实测值超出标准范围且未配置让步范围，需复检");
        if (upperLimit != null && testValue != null && testValue.compareTo(upperLimit) > 0) {
            sb.append("，超上限").append(deviation.abs().stripTrailingZeros().toPlainString());
        } else if (lowerLimit != null && testValue != null && testValue.compareTo(lowerLimit) < 0) {
            sb.append("，低于下限").append(deviation.abs().stripTrailingZeros().toPlainString());
        }
        if (lowerLimit != null) {
            sb.append("，下限=").append(lowerLimit.stripTrailingZeros().toPlainString());
        }
        if (upperLimit != null) {
            sb.append("，上限=").append(upperLimit.stripTrailingZeros().toPlainString());
        }
        return sb.toString();
    }

    private List<JudgmentOutput.StandardConflictItem> toOutputConflicts(List<StandardConflictDraftVO> drafts) {
        if (drafts == null || drafts.isEmpty()) {
            return new ArrayList<>();
        }
        return drafts.stream().map(draft -> JudgmentOutput.StandardConflictItem.builder()
                .conflictType(draft.getConflictType())
                .conflictLevel(draft.getConflictLevel())
                .status(draft.getStatus())
                .indicatorId(draft.getIndicatorId())
                .indicatorName(draft.getIndicatorName())
                .unit(draft.getUnit())
                .customerId(draft.getCustomerId())
                .variety(draft.getVariety())
                .grade(draft.getGrade())
                .productSpec(draft.getProductSpec())
                .inspectionDate(draft.getInspectionDate())
                .selectedStandardId(draft.getSelectedStandardId())
                .involvedStandardIds(draft.getInvolvedStandardIds())
                .conflictDetail(draft.getConflictDetail())
                .selectedPriority(draft.getSelectedPriority())
                .build()).collect(Collectors.toList());
    }

    private List<String> collectConflictStandardIds(List<JudgmentOutput.StandardConflictItem> conflicts) {
        if (conflicts == null || conflicts.isEmpty()) {
            return new ArrayList<>();
        }
        return conflicts.stream()
                .filter(conflict -> conflict.getInvolvedStandardIds() != null)
                .flatMap(conflict -> conflict.getInvolvedStandardIds().stream())
                .distinct()
                .collect(Collectors.toList());
    }
}
