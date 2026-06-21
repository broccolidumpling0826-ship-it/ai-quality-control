package com.jhict.quality.ai.evaluation;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jhict.quality.ai.config.AiProperties;
import com.jhict.quality.ai.rag.RagService;
import com.jhict.quality.entity.QcEvaluationRun;
import com.jhict.quality.entity.QcEvaluationSample;
import com.jhict.quality.enums.EvaluationRunStatus;
import com.jhict.quality.mapper.QcEvaluationRunMapper;
import com.jhict.quality.mapper.QcEvaluationSampleMapper;
import com.jhict.quality.service.api.AiJudgmentExplainService;
import com.jhict.quality.vo.AiJudgmentExplanationVO;
import com.jhict.quality.vo.EvaluationRunVO;
import com.jhict.quality.vo.RagQueryResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class EvaluationRunner {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Resource
    private QcEvaluationSampleMapper sampleMapper;

    @Resource
    private QcEvaluationRunMapper runMapper;

    @Resource
    private EvaluationScorer evaluationScorer;

    @Resource
    private RagService ragService;

    @Resource
    private AiJudgmentExplainService aiJudgmentExplainService;

    @Resource
    private AiProperties aiProperties;

    public EvaluationRunVO run(List<String> categories, String promptVersion) {
        String runNo = "EVAL-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        String now = LocalDateTime.now().format(FORMATTER);

        QcEvaluationRun run = new QcEvaluationRun();
        run.setRunNo(runNo);
        run.setPromptVersion(StringUtils.hasText(promptVersion) ? promptVersion : "v1.0.0");
        run.setModelName(aiProperties.getModel());
        run.setStatus(EvaluationRunStatus.RUNNING.getCode());
        run.setCreateDateTime(now);
        run.setUpdateDateTime(now);
        runMapper.insert(run);

        LambdaQueryWrapper<QcEvaluationSample> wrapper = new LambdaQueryWrapper<>();
        if (categories != null && !categories.isEmpty()) {
            wrapper.in(QcEvaluationSample::getCategory, categories);
        }
        List<QcEvaluationSample> samples = sampleMapper.selectList(wrapper);

        List<EvaluationScorer.SampleScore> scores = new ArrayList<>();
        for (QcEvaluationSample sample : samples) {
            long start = System.currentTimeMillis();
            JSONObject input = JSON.parseObject(sample.getInputPayload());
            JSONObject expected = JSON.parseObject(sample.getExpectedOutcome());
            JSONObject actual = executeSample(sample.getCategory(), input);
            EvaluationScorer.SampleScore score = evaluationScorer.scoreSample(
                    sample.getCategory(), input, expected, actual);
            score.setLatencyMs(System.currentTimeMillis() - start);
            if ("LOW_CONFIDENCE".equals(sample.getCategory())) {
                score.setManualReviewHit(actual.getBooleanValue("manualReviewRequired"));
            }
            scores.add(score);
        }

        EvaluationScorer.RunMetrics metrics = evaluationScorer.aggregate(scores);
        run.setStatus(EvaluationRunStatus.COMPLETED.getCode());
        run.setTotalSamples(metrics.getTotalSamples());
        run.setPassedSamples(metrics.getPassedSamples());
        run.setRulePassRate(metrics.getRulePassRate());
        run.setAiAccuracyRate(metrics.getAiAccuracyRate());
        run.setCitationHitRate(metrics.getCitationHitRate());
        run.setAvgLatencyMs(metrics.getAvgLatencyMs());
        run.setManualReviewHitRate(metrics.getManualReviewHitRate());
        run.setReportJson(JSON.toJSONString(metrics));
        run.setFinishedTime(LocalDateTime.now().format(FORMATTER));
        run.setUpdateDateTime(run.getFinishedTime());
        runMapper.updateById(run);

        return toVo(run);
    }

    private JSONObject executeSample(String category, JSONObject input) {
        JSONObject actual = new JSONObject();
        try {
            if ("INJECTION".equals(category) || input.containsKey("question")) {
                RagQueryResultVO rag = ragService.query(
                        input.getString("question"),
                        input.getString("standardId"),
                        input.getString("standardType"));
                actual.put("answer", rag.getAnswer());
                actual.put("found", rag.getFound());
                actual.put("citations", rag.getCitations());
                actual.put("degraded", rag.getDegraded());
            } else if (input.containsKey("judgmentId")) {
                AiJudgmentExplanationVO explain = aiJudgmentExplainService.explain(input.getString("judgmentId"));
                actual.put("narrativeText", explain.getNarrativeText());
                actual.put("confidenceLevel", explain.getConfidenceLevel());
                actual.put("manualReviewRequired", explain.getManualReviewRequired());
                actual.put("judgmentType", input.getString("judgmentType"));
                actual.put("citations", explain.getCitations());
            }
        } catch (Exception e) {
            log.warn("评测样例执行失败，category={}, error={}", category, e.getMessage());
            actual.put("error", e.getMessage());
        }
        return actual;
    }

    public EvaluationRunVO getByRunNo(String runNo) {
        QcEvaluationRun run = runMapper.findByRunNo(runNo);
        if (run == null) {
            return null;
        }
        return toVo(run);
    }

    private EvaluationRunVO toVo(QcEvaluationRun run) {
        EvaluationRunVO vo = new EvaluationRunVO();
        vo.setRunNo(run.getRunNo());
        vo.setStatus(run.getStatus());
        vo.setPromptVersion(run.getPromptVersion());
        vo.setModelName(run.getModelName());
        vo.setTotalSamples(run.getTotalSamples());
        vo.setPassedSamples(run.getPassedSamples());
        vo.setRulePassRate(run.getRulePassRate());
        vo.setAiAccuracyRate(run.getAiAccuracyRate());
        vo.setCitationHitRate(run.getCitationHitRate());
        vo.setAvgLatencyMs(run.getAvgLatencyMs());
        vo.setManualReviewHitRate(run.getManualReviewHitRate());
        vo.setReportJson(run.getReportJson());
        vo.setFinishedTime(run.getFinishedTime());
        return vo;
    }
}
