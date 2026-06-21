package com.jhict.quality.service.impl;

import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jhict.quality.ai.client.LlmClient;
import com.jhict.quality.ai.client.LlmRequest;
import com.jhict.quality.ai.client.LlmResponse;
import com.jhict.quality.ai.judgment.ConfidenceCalculator;
import com.jhict.quality.ai.prompt.PromptRegistry;
import com.jhict.quality.ai.prompt.PromptTemplate;
import com.jhict.quality.ai.rag.RagRetriever;
import com.jhict.quality.common.exception.ServiceException;
import com.jhict.quality.entity.*;
import com.jhict.quality.enums.AiCallSource;
import com.jhict.quality.mapper.*;
import com.jhict.quality.service.api.AiJudgmentExplainService;
import com.jhict.quality.service.api.JudgmentService;
import com.jhict.quality.vo.AiJudgmentExplanationVO;
import com.jhict.quality.vo.CitationVO;
import com.jhict.quality.vo.QcJudgmentResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AiJudgmentExplainServiceImpl implements AiJudgmentExplainService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final Map<String, String> DEMO_BATCH_MAP = new LinkedHashMap<>();

    static {
        DEMO_BATCH_MAP.put("QUALIFIED", "DEMO-QUALIFIED-001");
        DEMO_BATCH_MAP.put("UNQUALIFIED", "DEMO-UNQUALIFIED-001");
        DEMO_BATCH_MAP.put("CONCESSION", "DEMO-CONCESSION-001");
        DEMO_BATCH_MAP.put("CONFLICT", "DEMO-CONFLICT-001");
        DEMO_BATCH_MAP.put("DEMO-QUALIFIED-001", "DEMO-QUALIFIED-001");
        DEMO_BATCH_MAP.put("DEMO-UNQUALIFIED-001", "DEMO-UNQUALIFIED-001");
        DEMO_BATCH_MAP.put("DEMO-CONCESSION-001", "DEMO-CONCESSION-001");
        DEMO_BATCH_MAP.put("DEMO-CONFLICT-001", "DEMO-CONFLICT-001");
    }

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Resource
    private JudgmentService judgmentService;

    @Resource
    private QcAiJudgmentExplanationMapper explanationMapper;

    @Resource
    private QcInspectionRecordMapper inspectionRecordMapper;

    @Resource
    private QcJudgmentResultMapper judgmentResultMapper;

    @Resource
    private QcQualityStandardMapper qualityStandardMapper;

    @Resource
    private RagRetriever ragRetriever;

    @Resource
    private LlmClient llmClient;

    @Resource
    private PromptRegistry promptRegistry;

    @Resource
    private ConfidenceCalculator confidenceCalculator;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiJudgmentExplanationVO explain(String judgmentId) {
        QcJudgmentResultVO baseline = judgmentService.getExplanation(judgmentId);
        return buildAndPersist(judgmentId, baseline);
    }

    @Override
    public AiJudgmentExplanationVO explainByDemoCode(String demoCode) {
        String batchNo = DEMO_BATCH_MAP.get(demoCode);
        if (!StringUtils.hasText(batchNo)) {
            throw new ServiceException("未知演示编码：" + demoCode);
        }
        QcInspectionRecord record = inspectionRecordMapper.selectOne(
                new LambdaQueryWrapper<QcInspectionRecord>()
                        .eq(QcInspectionRecord::getBatchNo, batchNo)
                        .last("LIMIT 1"));
        if (record == null) {
            throw new ServiceException("演示批次不存在：" + batchNo);
        }
        QcJudgmentResult judgment = judgmentResultMapper.selectOne(
                new LambdaQueryWrapper<QcJudgmentResult>()
                        .eq(QcJudgmentResult::getRecordId, record.getId())
                        .eq(QcJudgmentResult::getIsFinal, 1)
                        .last("LIMIT 1"));
        if (judgment == null) {
            throw new ServiceException("演示批次无判定结论");
        }
        return explain(judgment.getId());
    }

    private AiJudgmentExplanationVO buildAndPersist(String judgmentId, QcJudgmentResultVO baseline) {
        String baselineJson;
        try {
            baselineJson = objectMapper.writeValueAsString(baseline);
        } catch (Exception e) {
            baselineJson = "{}";
        }

        ConfidenceCalculator.ConfidenceResult confidence = confidenceCalculator.calculate(baseline);
        List<CitationVO> citations = retrieveCitations(baseline);
        String citationIdsJson = JSON.toJSONString(citations.stream().map(CitationVO::getChunkId).collect(Collectors.toList()));

        PromptTemplate template = promptRegistry.getActiveTemplate("judgment-explain");
        String citationsText = citations.stream()
                .map(c -> c.getSectionRef() + ": " + c.getHighlightText())
                .collect(Collectors.joining("\n"));

        LlmRequest request = LlmRequest.builder()
                .callSource(AiCallSource.JUDGMENT_EXPLAIN)
                .promptKey("judgment-explain")
                .systemPrompt(template != null ? template.getSystem() : null)
                .userPrompt(template != null
                        ? template.renderUser("baselineJson", baselineJson, "citations", citationsText)
                        : baselineJson)
                .bizRefId(judgmentId)
                .citationIdsJson(citationIdsJson)
                .build();

        LlmResponse response = llmClient.chat(request);
        boolean degraded = response.isDegraded() || !response.isSuccess();
        String narrative = degraded ? buildBaselineNarrative(baseline, confidence) : response.getContent();
        if (!StringUtils.hasText(narrative)) {
            narrative = buildBaselineNarrative(baseline, confidence);
            degraded = true;
        }

        QcAiJudgmentExplanation existing = explanationMapper.findByJudgmentId(judgmentId);
        QcAiJudgmentExplanation entity = existing != null ? existing : new QcAiJudgmentExplanation();
        entity.setJudgmentId(judgmentId);
        entity.setNarrativeText(narrative);
        entity.setConfidenceLevel(confidence.getLevel().getCode());
        entity.setManualReviewRequired(confidence.isManualReviewRequired() ? 1 : 0);
        entity.setDegraded(degraded ? 1 : 0);
        entity.setBaselineJson(baselineJson);
        entity.setCitationIds(citationIdsJson);
        entity.setAuditLogId(response.getAuditLogId());
        String now = LocalDateTime.now().format(FORMATTER);
        if (existing == null) {
            entity.setCreateDateTime(now);
            entity.setUpdateDateTime(now);
            explanationMapper.insert(entity);
        } else {
            entity.setUpdateDateTime(now);
            explanationMapper.updateById(entity);
        }

        AiJudgmentExplanationVO vo = new AiJudgmentExplanationVO();
        vo.setJudgmentId(judgmentId);
        vo.setNarrativeText(narrative);
        vo.setConfidenceLevel(confidence.getLevel().getCode());
        vo.setManualReviewRequired(confidence.isManualReviewRequired());
        vo.setDegraded(degraded);
        vo.setBaselineJson(baselineJson);
        vo.setCitations(citations);
        vo.setAuditLogId(response.getAuditLogId());
        return vo;
    }

    private List<CitationVO> retrieveCitations(QcJudgmentResultVO baseline) {
        String query = baseline.getProductGrade() + " " + baseline.getProductVariety() + " 抗拉强度 延伸率";
        List<QcStandardDocumentChunk> chunks = ragRetriever.retrieve(query, null, null, 3);
        Map<String, QcQualityStandard> stdMap = chunks.isEmpty() ? Collections.emptyMap()
                : qualityStandardMapper.selectBatchIds(
                chunks.stream().map(QcStandardDocumentChunk::getStandardId).distinct().collect(Collectors.toList()))
                .stream().collect(Collectors.toMap(QcQualityStandard::getId, s -> s, (a, b) -> a));

        List<CitationVO> citations = new ArrayList<>();
        for (QcStandardDocumentChunk chunk : chunks) {
            CitationVO c = new CitationVO();
            c.setChunkId(chunk.getId());
            c.setStandardId(chunk.getStandardId());
            c.setSectionRef(chunk.getSectionRef());
            String text = chunk.getChunkText();
            c.setHighlightText(text.length() > 200 ? text.substring(0, 200) + "..." : text);
            QcQualityStandard std = stdMap.get(chunk.getStandardId());
            if (std != null) {
                c.setStandardName(StringUtils.hasText(std.getStandardName()) ? std.getStandardName() : std.getStandardCode());
            }
            citations.add(c);
        }
        return citations;
    }

    private String buildBaselineNarrative(QcJudgmentResultVO baseline, ConfidenceCalculator.ConfidenceResult confidence) {
        StringBuilder sb = new StringBuilder();
        sb.append("判定结论：").append(baseline.getJudgmentType()).append("。");
        if (StringUtils.hasText(baseline.getBatchNo())) {
            sb.append(" 批次 ").append(baseline.getBatchNo()).append("。");
        }
        if (baseline.getIndicatorDetails() != null && !baseline.getIndicatorDetails().isEmpty()) {
            sb.append(" 关键指标：");
            baseline.getIndicatorDetails().forEach(d -> {
                sb.append(d.getIndicatorName()).append("=").append(d.getMeasuredValue());
                if (StringUtils.hasText(d.getTriggeredRule())) {
                    sb.append("（").append(d.getTriggeredRule()).append("）");
                }
                sb.append("；");
            });
        }
        sb.append(" 置信度：").append(confidence.getLevel().getCode());
        if (confidence.isManualReviewRequired()) {
            sb.append("，建议人工复核。");
        }
        return sb.toString();
    }
}
