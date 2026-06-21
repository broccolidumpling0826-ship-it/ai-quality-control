package com.jhict.quality.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jhict.quality.entity.QcJudgmentEvidence;
import com.jhict.quality.entity.QcJudgmentResult;
import com.jhict.quality.mapper.QcIndicatorItemMapper;
import com.jhict.quality.mapper.QcJudgmentEvidenceMapper;
import com.jhict.quality.mapper.QcJudgmentResultMapper;
import com.jhict.quality.service.api.AiAssessmentService;
import com.jhict.quality.service.api.StatisticsService;
import com.jhict.quality.service.api.StandardConflictService;
import com.jhict.quality.vo.IndicatorDistributionVO;
import com.jhict.quality.vo.StatisticsOverviewVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class StatisticsServiceImpl implements StatisticsService {

    @Resource
    private QcJudgmentResultMapper judgmentResultMapper;

    @Resource
    private QcJudgmentEvidenceMapper judgmentEvidenceMapper;

    @Resource
    private QcIndicatorItemMapper indicatorItemMapper;

    @Resource
    private AiAssessmentService aiAssessmentService;

    @Resource
    private StandardConflictService standardConflictService;

    @Override
    public StatisticsOverviewVO getOverview(String timeStart, String timeEnd) {
        // 使用 MySQL 5.7 兼容的 COUNT(CASE WHEN) 写法
        QueryWrapper<QcJudgmentResult> wrapper = new QueryWrapper<QcJudgmentResult>()
                .eq("is_final", 1)
                .select(
                        "COUNT(*) AS totalInspection",
                        "COUNT(CASE WHEN judgment_type = 'QUALIFIED' THEN 1 END) AS qualifiedCount",
                        "COUNT(CASE WHEN judgment_type = 'UNQUALIFIED' THEN 1 END) AS unqualifiedCount",
                        "COUNT(CASE WHEN judgment_type = 'NEED_REINSPECTION' THEN 1 END) AS needReinspectionCount",
                        "COUNT(CASE WHEN judgment_type = 'CAN_CONCESSION' THEN 1 END) AS canConcessionCount"
                );
        if (StringUtils.hasText(timeStart)) {
            wrapper.ge("judgment_time", timeStart);
        }
        if (StringUtils.hasText(timeEnd)) {
            wrapper.le("judgment_time", timeEnd);
        }

        Map<String, Object> row = judgmentResultMapper.selectMaps(wrapper)
                .stream().findFirst().orElse(new HashMap<>());

        long total = toLong(row.get("totalInspection"));
        long qualified = toLong(row.get("qualifiedCount"));
        long unqualified = toLong(row.get("unqualifiedCount"));
        long needReinspection = toLong(row.get("needReinspectionCount"));
        long canConcession = toLong(row.get("canConcessionCount"));

        StatisticsOverviewVO vo = new StatisticsOverviewVO();
        vo.setTotalInspection(total);
        vo.setQualifiedCount(qualified);
        vo.setUnqualifiedCount(unqualified);
        vo.setNeedReinspectionCount(needReinspection);
        vo.setCanConcessionCount(canConcession);
        vo.setUnqualifiedRate(calcRate(unqualified, total));
        vo.setReinspectionRate(calcRate(needReinspection, total));
        vo.setConcessionRate(calcRate(canConcession, total));
        long conflictCount = standardConflictService.countConflicts(timeStart, timeEnd);
        long conflictResolvedCount = standardConflictService.countResolvedConflicts(timeStart, timeEnd);
        long aiAssessmentCount = aiAssessmentService.countAssessments(timeStart, timeEnd);
        long aiAdoptedCount = aiAssessmentService.countAdoptedAssessments(timeStart, timeEnd);
        long aiHandledCount = aiAssessmentService.countHandledAssessments(timeStart, timeEnd);
        long lowConfidenceAiCount = aiAssessmentService.countLowConfidenceAssessments(timeStart, timeEnd);
        vo.setStandardConflictCount(conflictCount);
        vo.setStandardConflictRate(calcRate(conflictCount, total));
        vo.setStandardConflictResolvedCount(conflictResolvedCount);
        vo.setConflictRemediationRate(calcRate(conflictResolvedCount, conflictCount));
        vo.setAiAssessmentCount(aiAssessmentCount);
        vo.setAiAdoptedCount(aiAdoptedCount);
        vo.setLowConfidenceAiCount(lowConfidenceAiCount);
        vo.setAiAdoptionRate(calcRate(aiAdoptedCount, aiAssessmentCount));
        vo.setAiHitRate(calcRate(aiAdoptedCount, aiHandledCount));
        vo.setManualReviewHandleRate(calcRate(aiHandledCount, aiAssessmentCount));
        vo.setAnalyticsTrendSummary("AI命中率按已处理建议中的采纳占比计算；人工复核处理率按已处理AI评估/AI评估总数计算；冲突裁决闭环率按已裁决冲突/冲突总数计算。");
        vo.setEvaluationCaseCount(30L);
        vo.setEvaluationSummary("P0评测集30条：正常10、边界/异常10、低置信/拒答5、Prompt注入/安全5。");

        return vo;
    }

    @Override
    public List<IndicatorDistributionVO> getIndicatorDistribution(String timeStart, String timeEnd) {
        // 查 JudgmentEvidence 中 isPassed=0 的数据，按 indicator_id 分组统计
        QueryWrapper<QcJudgmentEvidence> wrapper = new QueryWrapper<QcJudgmentEvidence>()
                .eq("is_passed", 0)
                .select("indicator_id", "COUNT(*) AS failCount")
                .groupBy("indicator_id")
                .orderByDesc("failCount");

        // 如果有时间范围，需要 join 判定结论表过滤（这里通过子查询方式处理）
        // MySQL 5.7 兼容，直接查全量，由 timeStart/timeEnd 过滤 evidence 的 create_date_time
        if (StringUtils.hasText(timeStart)) {
            wrapper.ge("create_date_time", timeStart);
        }
        if (StringUtils.hasText(timeEnd)) {
            wrapper.le("create_date_time", timeEnd);
        }

        List<Map<String, Object>> rows = judgmentEvidenceMapper.selectMaps(wrapper);

        // 查全部总数（用于计算不合格率分母）
        QueryWrapper<QcJudgmentEvidence> totalWrapper = new QueryWrapper<QcJudgmentEvidence>();
        if (StringUtils.hasText(timeStart)) {
            totalWrapper.ge("create_date_time", timeStart);
        }
        if (StringUtils.hasText(timeEnd)) {
            totalWrapper.le("create_date_time", timeEnd);
        }
        long totalEvidence = judgmentEvidenceMapper.selectCount(totalWrapper);

        // 构建指标名称映射
        Map<String, String> indicatorNameMap = new HashMap<>();
        if (!rows.isEmpty()) {
            List<String> indicatorIds = rows.stream()
                    .map(r -> String.valueOf(r.get("indicator_id")))
                    .collect(Collectors.toList());
            indicatorItemMapper.selectBatchIds(indicatorIds)
                    .forEach(item -> indicatorNameMap.put(item.getId(), item.getIndicatorName()));
        }

        return rows.stream().map(r -> {
            String indicatorId = String.valueOf(r.get("indicator_id"));
            long failCount = toLong(r.get("failCount"));

            IndicatorDistributionVO vo = new IndicatorDistributionVO();
            vo.setIndicatorName(indicatorNameMap.getOrDefault(indicatorId, indicatorId));
            vo.setFailCount(failCount);
            vo.setFailRate(calcRate(failCount, totalEvidence));
            return vo;
        }).collect(Collectors.toList());
    }

    // ---------- private helpers ----------

    private long toLong(Object val) {
        if (val == null) return 0L;
        if (val instanceof Number) return ((Number) val).longValue();
        try {
            return Long.parseLong(val.toString());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    private String calcRate(long numerator, long denominator) {
        if (denominator == 0) return "0.00%";
        return new BigDecimal(numerator)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(denominator), 2, RoundingMode.HALF_UP)
                .toPlainString() + "%";
    }
}
