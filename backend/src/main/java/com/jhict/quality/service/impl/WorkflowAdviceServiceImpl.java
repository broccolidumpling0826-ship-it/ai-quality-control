package com.jhict.quality.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jhict.quality.dto.AiAssessmentCreateCmd;
import com.jhict.quality.entity.QcAiAssessment;
import com.jhict.quality.service.api.AiAssessmentService;
import com.jhict.quality.service.api.JudgmentService;
import com.jhict.quality.service.api.WorkflowAdviceService;
import com.jhict.quality.vo.AiSourceReferenceVO;
import com.jhict.quality.vo.QcJudgmentResultVO;
import com.jhict.quality.vo.WorkflowAdviceVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class WorkflowAdviceServiceImpl implements WorkflowAdviceService {

    private static final String TYPE_REINSPECTION = "REINSPECTION_ADVICE";
    private static final String TYPE_REJUDGMENT = "REJUDGMENT_ADVICE";
    private static final String ACTION_REINSPECTION = "RECOMMEND_REINSPECTION";
    private static final String ACTION_REJUDGMENT = "RECOMMEND_REJUDGMENT";
    private static final String ACTION_NOT_RECOMMENDED = "NOT_RECOMMENDED";
    private static final String ACTION_WITHHELD = "WITHHELD";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Resource
    private JudgmentService judgmentService;

    @Resource
    private AiAssessmentService aiAssessmentService;

    @Override
    public WorkflowAdviceVO adviseReinspection(String judgmentId) {
        QcJudgmentResultVO judgment = judgmentService.getExplanationSnapshot(judgmentId);
        WorkflowAdviceVO advice = baseAdvice(TYPE_REINSPECTION, judgment);
        List<QcJudgmentResultVO.EvidenceVO> abnormal = abnormalEvidences(judgment);
        advice.setTriggerIndicators(toIndicatorLabels(abnormal, judgment));
        advice.setEvidenceSummary(buildEvidenceSummary(judgment, abnormal));

        if ("STANDARD_CONFLICT".equals(judgment.getJudgmentType())) {
            withhold(advice, "存在未裁决标准冲突，复检不能解决标准口径冲突");
        } else if (isLowConfidence(judgment)) {
            withhold(advice, "当前判定解释置信度低，系统不提供确定性复检建议");
        } else if (abnormal.isEmpty()) {
            advice.setRecommendedAction(ACTION_NOT_RECOMMENDED);
            advice.setSuggestedReason("未发现不合格或让步带异常指标，暂不建议发起复检");
            advice.setConfidenceLabel("HIGH");
            advice.setConfidenceScore(0.86D);
        } else {
            advice.setRecommendedAction(ACTION_REINSPECTION);
            advice.setSuggestedReason("异常指标 " + String.join("、", advice.getTriggerIndicators())
                    + " 存在偏离，建议人工发起复检确认样品代表性和检测稳定性");
            advice.setMustManualReview(true);
            if (!StringUtils.hasText(advice.getConfidenceLabel())) {
                advice.setConfidenceLabel("MEDIUM");
                advice.setConfidenceScore(0.72D);
            }
        }

        persistAdvice(advice, judgment);
        return advice;
    }

    @Override
    public WorkflowAdviceVO adviseRejudgment(String judgmentId) {
        QcJudgmentResultVO judgment = judgmentService.getExplanationSnapshot(judgmentId);
        WorkflowAdviceVO advice = baseAdvice(TYPE_REJUDGMENT, judgment);
        List<QcJudgmentResultVO.EvidenceVO> abnormal = abnormalEvidences(judgment);
        advice.setTriggerIndicators(toIndicatorLabels(abnormal, judgment));
        advice.setEvidenceSummary(buildEvidenceSummary(judgment, abnormal));
        advice.setAffectedScope("卷号 " + nullToDash(judgment.getCoilNo()) + " / 批次 " + nullToDash(judgment.getBatchNo()));

        if ("STANDARD_CONFLICT".equals(judgment.getJudgmentType())) {
            withhold(advice, "当前问题是标准冲突，应先走标准裁决，不建议直接发起改判");
        } else if (isLowConfidence(judgment)) {
            withhold(advice, "当前证据或引用不足，系统不提供确定性改判目标");
        } else if (hasConcessionIndicator(judgment)) {
            advice.setRecommendedAction(ACTION_REJUDGMENT);
            advice.setTargetJudgmentType("CAN_CONCESSION");
            advice.setSuggestedReason("存在处于让步带内的异常指标，可由人工评审是否从不合格改判为可让步");
            advice.setMustManualReview(true);
            advice.setConfidenceLabel(resolveConfidence(advice.getConfidenceLabel(), "MEDIUM"));
            advice.setConfidenceScore(resolveScore(advice.getConfidenceScore(), 0.70D));
        } else {
            advice.setRecommendedAction(ACTION_NOT_RECOMMENDED);
            advice.setSuggestedReason("未发现足以支持改判的新增证据或让步带证据，建议保持现有结论");
            advice.setConfidenceLabel(resolveConfidence(advice.getConfidenceLabel(), "HIGH"));
            advice.setConfidenceScore(resolveScore(advice.getConfidenceScore(), 0.84D));
        }

        persistAdvice(advice, judgment);
        return advice;
    }

    private WorkflowAdviceVO baseAdvice(String adviceType, QcJudgmentResultVO judgment) {
        WorkflowAdviceVO advice = new WorkflowAdviceVO();
        advice.setAdviceType(adviceType);
        advice.setJudgmentId(judgment.getJudgmentId());
        advice.setRecordId(judgment.getRecordId());
        advice.setWithheld(false);
        advice.setMustManualReview(false);
        advice.setMissingInfo(new ArrayList<>());
        advice.setEvidenceRefs(judgment.getCitations() == null ? new ArrayList<AiSourceReferenceVO>() : judgment.getCitations());
        advice.setConfidenceLabel(resolveConfidence(judgment.getConfidenceLabel(), "MEDIUM"));
        advice.setConfidenceScore(resolveScore(judgment.getConfidenceScore(), "HIGH".equals(advice.getConfidenceLabel()) ? 0.86D : 0.68D));
        if (Boolean.TRUE.equals(judgment.getCitationMissing())) {
            advice.getMissingInfo().add("缺少部分来源段落引用");
        }
        return advice;
    }

    private void withhold(WorkflowAdviceVO advice, String reason) {
        advice.setRecommendedAction(ACTION_WITHHELD);
        advice.setWithheld(true);
        advice.setMustManualReview(true);
        advice.setSuggestedReason(reason);
        advice.setConfidenceLabel("LOW");
        advice.setConfidenceScore(0.35D);
        advice.getMissingInfo().add(reason);
    }

    private List<QcJudgmentResultVO.EvidenceVO> abnormalEvidences(QcJudgmentResultVO judgment) {
        if (judgment.getEvidences() == null) {
            return new ArrayList<>();
        }
        return judgment.getEvidences().stream()
                .filter(e -> e.getIsPassed() != null && e.getIsPassed() == 0)
                .collect(Collectors.toList());
    }

    private List<String> toIndicatorLabels(List<QcJudgmentResultVO.EvidenceVO> abnormal,
                                           QcJudgmentResultVO judgment) {
        if (!abnormal.isEmpty()) {
            return abnormal.stream()
                    .map(e -> StringUtils.hasText(e.getIndicatorName()) ? e.getIndicatorName() : e.getIndicatorCode())
                    .filter(StringUtils::hasText)
                    .distinct()
                    .collect(Collectors.toList());
        }
        if (judgment.getIndicatorDetails() == null) {
            return new ArrayList<>();
        }
        return judgment.getIndicatorDetails().stream()
                .filter(i -> !"PASS".equals(i.getIndicatorResult()))
                .map(QcJudgmentResultVO.IndicatorDetailVO::getIndicatorName)
                .filter(StringUtils::hasText)
                .distinct()
                .collect(Collectors.toList());
    }

    private String buildEvidenceSummary(QcJudgmentResultVO judgment, List<QcJudgmentResultVO.EvidenceVO> abnormal) {
        if (abnormal.isEmpty()) {
            return "判定 " + judgment.getJudgmentType() + " 未发现异常指标证据。";
        }
        return abnormal.stream()
                .map(e -> String.format("%s 实测 %s，限值[%s,%s]，偏差 %s",
                        nullToDash(e.getIndicatorName()),
                        decimalToString(e.getTestValue()),
                        decimalToString(e.getLowerLimit()),
                        decimalToString(e.getUpperLimit()),
                        decimalToString(e.getDeviation())))
                .collect(Collectors.joining("；"));
    }

    private boolean hasConcessionIndicator(QcJudgmentResultVO judgment) {
        if ("CAN_CONCESSION".equals(judgment.getJudgmentType())) {
            return true;
        }
        if (judgment.getIndicatorDetails() == null) {
            return false;
        }
        return judgment.getIndicatorDetails().stream()
                .anyMatch(i -> "CONCESSION".equals(i.getIndicatorResult()));
    }

    private boolean isLowConfidence(QcJudgmentResultVO judgment) {
        return "LOW".equals(judgment.getConfidenceLabel()) || Boolean.TRUE.equals(judgment.getCitationMissing());
    }

    private void persistAdvice(WorkflowAdviceVO advice, QcJudgmentResultVO judgment) {
        try {
            Map<String, Object> input = new LinkedHashMap<>();
            input.put("judgmentId", judgment.getJudgmentId());
            input.put("recordId", judgment.getRecordId());
            input.put("judgmentType", judgment.getJudgmentType());
            input.put("triggerIndicators", advice.getTriggerIndicators());

            Map<String, Object> structured = new LinkedHashMap<>();
            structured.put("recommendedAction", advice.getRecommendedAction());
            structured.put("withheld", advice.getWithheld());
            structured.put("targetJudgmentType", advice.getTargetJudgmentType());
            structured.put("suggestedReason", advice.getSuggestedReason());
            structured.put("affectedScope", advice.getAffectedScope());
            structured.put("missingInfo", advice.getMissingInfo());

            AiAssessmentCreateCmd cmd = new AiAssessmentCreateCmd();
            cmd.setAssessmentType(advice.getAdviceType());
            cmd.setBusinessType("JUDGMENT");
            cmd.setBusinessId(judgment.getJudgmentId());
            cmd.setRelatedJudgmentId(judgment.getJudgmentId());
            cmd.setInputSnapshot(objectMapper.writeValueAsString(input));
            cmd.setReferencesJson(objectMapper.writeValueAsString(advice.getEvidenceRefs()));
            cmd.setModelProvider("RULE_TEMPLATE");
            cmd.setModelName("workflow-advice-v1");
            cmd.setPromptVersion("workflow-advice-v1");
            cmd.setRawOutput(advice.getSuggestedReason());
            cmd.setStructuredOutput(objectMapper.writeValueAsString(structured));
            cmd.setRiskLevel(Boolean.TRUE.equals(advice.getWithheld()) ? "MANUAL_REVIEW" : null);
            cmd.setConfidenceLabel(advice.getConfidenceLabel());
            cmd.setConfidenceScore(BigDecimal.valueOf(advice.getConfidenceScore()));
            cmd.setConfidenceFactors(objectMapper.writeValueAsString(advice.getMissingInfo()));
            cmd.setDegradationSource("RULE_TEMPLATE");
            cmd.setCacheHit(0);
            QcAiAssessment assessment = aiAssessmentService.create(cmd);
            advice.setAssessmentId(assessment.getId());
        } catch (Exception e) {
            log.warn("保存流程建议AI评估失败，judgmentId={}, adviceType={}", judgment.getJudgmentId(), advice.getAdviceType(), e);
        }
    }

    private String resolveConfidence(String actual, String fallback) {
        return StringUtils.hasText(actual) ? actual : fallback;
    }

    private Double resolveScore(Double actual, Double fallback) {
        return actual == null ? fallback : actual;
    }

    private String decimalToString(BigDecimal value) {
        return value == null ? "-" : value.stripTrailingZeros().toPlainString();
    }

    private String nullToDash(String value) {
        return StringUtils.hasText(value) ? value : "-";
    }
}
