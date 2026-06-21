package com.jhict.quality.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jhict.quality.common.constant.JudgmentExplainConstants;
import com.jhict.quality.dto.AiAssessmentCreateCmd;
import com.jhict.quality.dto.ConcessionRiskAssessCmd;
import com.jhict.quality.dto.StandardClausePageQuery;
import com.jhict.quality.entity.QcAiAssessment;
import com.jhict.quality.enums.JudgmentType;
import com.jhict.quality.gateway.model.ModelChatRequest;
import com.jhict.quality.gateway.model.ModelChatResponse;
import com.jhict.quality.gateway.model.ModelGateway;
import com.jhict.quality.gateway.model.ModelMessage;
import com.jhict.quality.service.api.AiAssessmentService;
import com.jhict.quality.service.api.AlternativeStockService;
import com.jhict.quality.service.api.ConcessionRiskService;
import com.jhict.quality.service.api.CustomerUsageProfileService;
import com.jhict.quality.service.api.JudgmentService;
import com.jhict.quality.service.api.StandardDocumentService;
import com.jhict.quality.vo.AiSourceReferenceVO;
import com.jhict.quality.vo.AlternativeStockVO;
import com.jhict.quality.vo.ConcessionRiskAssessmentVO;
import com.jhict.quality.vo.CustomerUsageProfileVO;
import com.jhict.quality.vo.QcJudgmentResultVO;
import com.jhict.quality.vo.StandardClauseVO;
import com.jhict.quality.vo.StandardConflictVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ConcessionRiskServiceImpl implements ConcessionRiskService {

    private static final String PROMPT_VERSION = "concession-risk-v1";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Resource
    private JudgmentService judgmentService;

    @Resource
    private CustomerUsageProfileService customerUsageProfileService;

    @Resource
    private AlternativeStockService alternativeStockService;

    @Resource
    private StandardDocumentService standardDocumentService;

    @Resource
    private ModelGateway modelGateway;

    @Resource
    private AiAssessmentService aiAssessmentService;

    @Override
    public ConcessionRiskAssessmentVO assess(ConcessionRiskAssessCmd cmd) {
        QcJudgmentResultVO judgment = judgmentService.getExplanationSnapshot(cmd.getJudgmentId());
        ConcessionRiskAssessmentVO result = initResult(cmd.getJudgmentId());
        resolveUsage(cmd, judgment, result);
        result.setEvidenceRefs(loadRiskEvidence(judgment));
        result.setAlternativeStocks(alternativeStockService.findAvailable(
                judgment.getProductVariety(), judgment.getProductGrade(), judgment.getProductSpec(),
                cmd.getDeliveryWindowDays() == null ? 7 : cmd.getDeliveryWindowDays()));

        applyBaselineRules(judgment, result);
        result.setNarrativeExplanation(buildNarrative(judgment, result));
        fillAiWordingIfAvailable(judgment, result);
        persistAssessment(cmd, judgment, result);
        return result;
    }

    private ConcessionRiskAssessmentVO initResult(String judgmentId) {
        ConcessionRiskAssessmentVO result = new ConcessionRiskAssessmentVO();
        result.setJudgmentId(judgmentId);
        result.setRiskLevel("LOW");
        result.setMustReview(false);
        result.setMissingInfo(new ArrayList<>());
        result.setSuggestedConditions(new ArrayList<>());
        result.setBlockingReasons(new ArrayList<>());
        result.setEvidenceRefs(new ArrayList<>());
        result.setAlternativeStocks(new ArrayList<>());
        result.setConfidenceLabel("MEDIUM");
        result.setConfidenceScore(0.65D);
        result.setDegradationSource("RULE_TEMPLATE");
        return result;
    }

    private void resolveUsage(ConcessionRiskAssessCmd cmd, QcJudgmentResultVO judgment,
                              ConcessionRiskAssessmentVO result) {
        if (StringUtils.hasText(cmd.getCustomerUsage())) {
            result.setCustomerUsage(cmd.getCustomerUsage());
            result.setUsageRiskCategory(StringUtils.hasText(cmd.getUsageRiskCategory())
                    ? cmd.getUsageRiskCategory() : "NORMAL");
            return;
        }
        CustomerUsageProfileVO profile = customerUsageProfileService.findBestMatch(
                judgment.getCustomerId(), judgment.getProductVariety(), judgment.getProductGrade());
        if (profile != null) {
            result.setCustomerUsage(profile.getDefaultUsage());
            result.setUsageRiskCategory(profile.getRiskCategory());
        } else {
            result.getMissingInfo().add("缺少客户用途，需要人工补充 customerUsage");
        }
    }

    private List<AiSourceReferenceVO> loadRiskEvidence(QcJudgmentResultVO judgment) {
        Map<String, AiSourceReferenceVO> refs = new LinkedHashMap<>();
        for (QcJudgmentResultVO.MatchedStandardVO standard : judgment.getMatchedStandards() == null
                ? Collections.<QcJudgmentResultVO.MatchedStandardVO>emptyList() : judgment.getMatchedStandards()) {
            StandardClausePageQuery query = new StandardClausePageQuery();
            query.setStandardId(standard.getStandardId());
            query.setKeyword("让步");
            query.setPageNum(1);
            query.setPageSize(5);
            addRefs(refs, query);
        }
        StandardClausePageQuery caseQuery = new StandardClausePageQuery();
        caseQuery.setSourceType("COMPLAINT");
        caseQuery.setVariety(judgment.getProductVariety());
        caseQuery.setGrade(judgment.getProductGrade());
        caseQuery.setPageNum(1);
        caseQuery.setPageSize(5);
        addRefs(refs, caseQuery);
        return refs.values().stream().limit(8).collect(Collectors.toList());
    }

    private void addRefs(Map<String, AiSourceReferenceVO> refs, StandardClausePageQuery query) {
        try {
            for (StandardClauseVO clause : standardDocumentService.pageClauses(query).getRecords()) {
                AiSourceReferenceVO ref = new AiSourceReferenceVO();
                ref.setClauseId(clause.getId());
                ref.setDocumentId(clause.getDocumentId());
                ref.setSourceType(clause.getSourceType());
                ref.setStandardCode(clause.getStandardCode());
                ref.setStandardName(clause.getStandardName());
                ref.setVersionNo(clause.getVersionNo());
                ref.setClauseNo(clause.getClauseNo());
                ref.setPageNo(clause.getPageNo());
                ref.setParagraphText(clause.getParagraphText());
                ref.setScore(1.0D);
                refs.putIfAbsent(clause.getId(), ref);
            }
        } catch (Exception e) {
            log.warn("查询让步风险证据失败，sourceType={}, standardId={}, error={}",
                    query.getSourceType(), query.getStandardId(), e.getMessage());
        }
    }

    private void applyBaselineRules(QcJudgmentResultVO judgment, ConcessionRiskAssessmentVO result) {
        if (!JudgmentType.CAN_CONCESSION.getCode().equals(judgment.getJudgmentType())) {
            block(result, "仅 CAN_CONCESSION 判定可生成正常让步风险评估");
        }
        boolean unresolvedConflict = judgment.getConflicts() != null && judgment.getConflicts().stream()
                .anyMatch(c -> "BLOCKING".equals(c.getConflictLevel()) && "PENDING".equals(c.getStatus()));
        if (unresolvedConflict || JudgmentType.STANDARD_CONFLICT.getCode().equals(judgment.getJudgmentType())) {
            block(result, "存在未裁决标准冲突");
        }
        if (!result.getMissingInfo().isEmpty()) {
            block(result, "关键信息缺失：" + String.join("；", result.getMissingInfo()));
        }
        boolean hasConcessionEvidence = judgment.getEvidences() != null && judgment.getEvidences().stream()
                .anyMatch(e -> e.getTriggerRule() != null
                        && e.getTriggerRule().contains(JudgmentExplainConstants.TRIGGER_RULE_CONCESSION_MARKER));
        if (!hasConcessionEvidence) {
            block(result, "缺少让步范围触发依据");
        }
        boolean highUsage = "SAFETY_CRITICAL".equals(result.getUsageRiskCategory())
                || "HIGH_FORMING".equals(result.getUsageRiskCategory());
        boolean mechanicalDeviation = judgment.getEvidences() != null && judgment.getEvidences().stream()
                .anyMatch(e -> containsAny(e.getIndicatorName(), "强度", "延伸", "伸长", "Rm", "A"));
        if (!"BLOCKED".equals(result.getRiskLevel()) && highUsage && mechanicalDeviation) {
            result.setRiskLevel("HIGH");
            result.setMustReview(true);
            result.getSuggestedConditions().add("用途为高风险场景，需质量经理复核并限定使用部位/批量");
        }
        boolean complaintFound = result.getEvidenceRefs().stream()
                .anyMatch(ref -> "COMPLAINT".equals(ref.getSourceType()) || "CASE".equals(ref.getSourceType()));
        if (!"BLOCKED".equals(result.getRiskLevel()) && complaintFound) {
            result.setRiskLevel("HIGH".equals(result.getRiskLevel()) ? "HIGH" : "MEDIUM");
            result.setMustReview(true);
            result.getSuggestedConditions().add("发现相似投诉/案例，需复核历史问题和客户风险");
        }
        if (!result.getAlternativeStocks().isEmpty()) {
            result.getSuggestedConditions().add("存在可替代合格库存，建议优先评估替代发运而不是直接让步");
        } else {
            result.getMissingInfo().add("未找到可替代合格库存");
            result.setConfidenceLabel("LOW");
            result.setConfidenceScore(0.45D);
        }
        if ("LOW".equals(result.getRiskLevel()) && !result.getSuggestedConditions().isEmpty()) {
            result.setRiskLevel("MEDIUM");
        }
        if ("BLOCKED".equals(result.getRiskLevel())) {
            result.setConfidenceLabel("LOW");
            result.setConfidenceScore(0.25D);
        }
    }

    private void block(ConcessionRiskAssessmentVO result, String reason) {
        result.setRiskLevel("BLOCKED");
        result.setMustReview(true);
        result.getBlockingReasons().add(reason);
    }

    private String buildNarrative(QcJudgmentResultVO judgment, ConcessionRiskAssessmentVO result) {
        if ("BLOCKED".equals(result.getRiskLevel())) {
            return "当前信息不足或存在阻断条件，不能给出明确让步建议，必须人工质量评审。原因："
                    + String.join("；", result.getBlockingReasons());
        }
        return "基于客户用途、偏差指标、历史案例和替代资源，当前让步风险等级为 "
                + result.getRiskLevel()
                + "。建议条件："
                + (result.getSuggestedConditions().isEmpty() ? "按现行业务审批执行" : String.join("；", result.getSuggestedConditions()));
    }

    private void fillAiWordingIfAvailable(QcJudgmentResultVO judgment, ConcessionRiskAssessmentVO result) {
        if (!modelGateway.enabled() || "BLOCKED".equals(result.getRiskLevel())) {
            return;
        }
        ModelChatResponse response = modelGateway.chat(ModelChatRequest.builder()
                .businessType("CONCESSION_RISK")
                .businessId(judgment.getJudgmentId())
                .promptVersion(PROMPT_VERSION)
                .systemPrompt("你是钢铁质量让步风险说明助手。只能改写已给出的基线结论，不得改变风险等级或新增审批结论。")
                .messages(Collections.singletonList(ModelMessage.builder()
                        .role("user")
                        .content("基线结论：" + result.getNarrativeExplanation()
                                + "\n风险等级：" + result.getRiskLevel()
                                + "\n建议条件：" + result.getSuggestedConditions())
                        .build()))
                .temperature(0.1D)
                .maxTokens(500)
                .build());
        if (response != null && response.isSuccess() && StringUtils.hasText(response.getContent())) {
            result.setNarrativeExplanation(response.getContent());
            result.setDegradationSource("GENERATED");
        }
    }

    private void persistAssessment(ConcessionRiskAssessCmd cmd, QcJudgmentResultVO judgment,
                                   ConcessionRiskAssessmentVO result) {
        try {
            AiAssessmentCreateCmd createCmd = new AiAssessmentCreateCmd();
            createCmd.setAssessmentType("CONCESSION_RISK");
            createCmd.setBusinessType("QC_JUDGMENT_RESULT");
            createCmd.setBusinessId(cmd.getJudgmentId());
            createCmd.setRelatedJudgmentId(cmd.getJudgmentId());
            createCmd.setInputSnapshot(objectMapper.writeValueAsString(cmd));
            createCmd.setReferencesJson(objectMapper.writeValueAsString(result.getEvidenceRefs()));
            createCmd.setPromptVersion(PROMPT_VERSION);
            createCmd.setRawOutput(result.getNarrativeExplanation());
            createCmd.setStructuredOutput(objectMapper.writeValueAsString(result));
            createCmd.setRiskLevel(result.getRiskLevel());
            createCmd.setConfidenceLabel(result.getConfidenceLabel());
            createCmd.setConfidenceScore(BigDecimal.valueOf(result.getConfidenceScore()));
            createCmd.setConfidenceFactors(objectMapper.writeValueAsString(result.getMissingInfo()));
            createCmd.setDegradationSource(result.getDegradationSource());
            createCmd.setCacheHit(0);
            QcAiAssessment assessment = aiAssessmentService.create(createCmd);
            result.setAssessmentId(assessment.getId());
        } catch (Exception e) {
            log.warn("保存让步风险AI评估失败，judgmentId={}, error={}", judgment.getJudgmentId(), e.getMessage());
        }
    }

    private boolean containsAny(String value, String... keywords) {
        if (!StringUtils.hasText(value)) {
            return false;
        }
        for (String keyword : keywords) {
            if (value.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
