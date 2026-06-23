package com.jhict.quality.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jhict.quality.dto.AiAssessmentCreateCmd;
import com.jhict.quality.dto.CertQaQueryCmd;
import com.jhict.quality.dto.QcJudgmentPageQuery;
import com.jhict.quality.entity.QcAiCache;
import com.jhict.quality.enums.JudgmentType;
import com.jhict.quality.gateway.model.ModelChatRequest;
import com.jhict.quality.gateway.model.ModelChatResponse;
import com.jhict.quality.gateway.model.ModelGateway;
import com.jhict.quality.gateway.model.ModelMessage;
import com.jhict.quality.service.api.AiAssessmentService;
import com.jhict.quality.service.api.AiCacheService;
import com.jhict.quality.service.api.CertDataService;
import com.jhict.quality.service.api.CertQaService;
import com.jhict.quality.service.api.JudgmentService;
import com.jhict.quality.service.support.rag.CitationReferenceSupport;
import com.jhict.quality.service.support.rag.CitationReferenceSupport.CitationPromptStyle;
import com.jhict.quality.vo.AiSourceReferenceVO;
import com.jhict.quality.vo.CertQaAnswerVO;
import com.jhict.quality.vo.QcJudgmentListVO;
import com.jhict.quality.vo.QcJudgmentResultVO;
import com.jhict.quality.vo.QcQualityCertDataVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CertQaServiceImpl implements CertQaService {

    private static final String PROMPT_VERSION = "cert-qa-v3";
    private static final String ASSESSMENT_TYPE_CERT_QA = "CERT_QA";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Resource
    private AiCacheService aiCacheService;

    @Resource
    private CertDataService certDataService;

    @Resource
    private JudgmentService judgmentService;

    @Resource
    private ModelGateway modelGateway;

    @Resource
    private AiAssessmentService aiAssessmentService;

    @Override
    public CertQaAnswerVO answer(CertQaQueryCmd cmd) {
        CertQaAnswerVO answer = initAnswer();
        if (!StringUtils.hasText(cmd.getCoilNo()) && !StringUtils.hasText(cmd.getBatchNo())) {
            return refuse(answer, "必须提供卷号或批次号");
        }

        QcQualityCertDataVO cert = loadLatestCert(cmd);
        answer.setCertificateSnapshotFound(cert != null);
        QcJudgmentResultVO judgment = loadFinalJudgment(cmd);
        if (judgment == null) {
            return refuse(answer, "未找到卷号/批次对应的最终判定");
        }

        List<QcJudgmentResultVO.EvidenceVO> basis = resolveIndicatorBasis(cmd.getQuestion(), judgment);
        List<AiSourceReferenceVO> citations = CitationReferenceSupport.rankAndLimit(
                judgment.getCitations() == null ? Collections.emptyList() : judgment.getCitations(),
                basis,
                8);

        CertQaAnswerVO cached = tryCache(cmd, citations, basis);
        if (cached != null) {
            return cached;
        }

        answer.setCitations(citations);
        answer.setIndicatorBasis(basis);
        applyStateGate(answer, cert, judgment);
        if (Boolean.TRUE.equals(answer.getRefused())) {
            return answer;
        }

        String ruleAnswer = buildAnswerText(cmd.getQuestion(), cert, judgment, basis);
        fillGeneratedAnswer(cmd, answer, cert, judgment, ruleAnswer);
        return answer;
    }

    private void fillGeneratedAnswer(CertQaQueryCmd cmd,
                                     CertQaAnswerVO answer,
                                     QcQualityCertDataVO cert,
                                     QcJudgmentResultVO judgment,
                                     String ruleAnswer) {
        answer.setAnswer(ruleAnswer);
        List<AiSourceReferenceVO> citations = answer.getCitations() == null
                ? Collections.emptyList() : answer.getCitations();
        if (!modelGateway.enabled()) {
            applyRuleDegradation(answer, citations, ruleAnswer);
            return;
        }
        ModelChatResponse response = modelGateway.chat(buildCertQaChatRequest(
                cmd, cert, judgment, answer.getIndicatorBasis(), citations, ruleAnswer));
        String trusted = CitationReferenceSupport.acceptTrustedCitedOutput(
                response != null ? response.getContent() : null, citations, answer.getIndicatorBasis());
        if (StringUtils.hasText(trusted)) {
            answer.setAnswer(trusted);
            answer.setConfidenceLabel(citations.isEmpty() ? "MEDIUM" : "HIGH");
            answer.setDegradationSource("GENERATED");
            persistCertQaAssessment(cmd, judgment, answer, true);
            return;
        }
        if (response != null && response.isSuccess()) {
            log.warn("质保书问答引用校验未通过，coilNo={}, batchNo={}", cmd.getCoilNo(), cmd.getBatchNo());
        } else if (response != null) {
            log.warn("质保书问答生成失败，coilNo={}, batchNo={}, error={}",
                    cmd.getCoilNo(), cmd.getBatchNo(), response.getErrorMessage());
        }
        applyRuleDegradation(answer, citations, ruleAnswer);
        persistCertQaAssessment(cmd, judgment, answer, false);
    }

    private void applyRuleDegradation(CertQaAnswerVO answer,
                                      List<AiSourceReferenceVO> citations,
                                      String ruleAnswer) {
        answer.setAnswer(ruleAnswer);
        if (citations.isEmpty()) {
            answer.setConfidenceLabel("MEDIUM");
            answer.setDegradationSource("RULE_TEMPLATE");
            answer.setAnswer(ruleAnswer + " 未找到可引用的来源段落，以上仅基于结构化判定快照。");
        } else {
            answer.setConfidenceLabel("MEDIUM");
            answer.setDegradationSource("RAW_RETRIEVAL");
        }
    }

    private ModelChatRequest buildCertQaChatRequest(CertQaQueryCmd cmd,
                                                    QcQualityCertDataVO cert,
                                                    QcJudgmentResultVO judgment,
                                                    List<QcJudgmentResultVO.EvidenceVO> basis,
                                                    List<AiSourceReferenceVO> citations,
                                                    String ruleAnswer) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("用户问题：").append(cmd.getQuestion()).append("\n\n");
        prompt.append("结构化事实摘要：\n").append(ruleAnswer).append("\n\n");
        if (cert != null) {
            prompt.append("质保书快照状态：").append(cert.getStatus()).append("\n");
        }
        prompt.append("最终判定：").append(judgment.getJudgmentType()).append("\n");
        prompt.append("是否非最终态：").append(Boolean.TRUE.equals(answerNonFinal(cert, judgment)) ? "是" : "否").append("\n");
        if (basis != null && !basis.isEmpty()) {
            prompt.append("\n指标依据：\n");
            for (QcJudgmentResultVO.EvidenceVO evidence : basis) {
                prompt.append("- ")
                        .append(nullToEmpty(evidence.getIndicatorName()))
                        .append(" 实测=").append(evidence.getTestValue())
                        .append(" 下限=").append(evidence.getLowerLimit())
                        .append(" 上限=").append(evidence.getUpperLimit())
                        .append(" 偏差=").append(evidence.getDeviation())
                        .append(" 规则=").append(nullToEmpty(evidence.getTriggerRule()))
                        .append("\n");
            }
        }
        CitationReferenceSupport.appendNumberedCitationBlock(prompt, citations);
        CitationReferenceSupport.appendCitationAnswerRules(prompt, CitationPromptStyle.CERT_QA);
        if (Boolean.TRUE.equals(answerNonFinal(cert, judgment))) {
            prompt.append("当前为不可正式出证或非最终状态，回答中必须明确说明。");
        }
        return ModelChatRequest.builder()
                .businessType(ASSESSMENT_TYPE_CERT_QA)
                .businessId(judgment.getJudgmentId())
                .promptVersion(PROMPT_VERSION)
                .systemPrompt("你是钢铁质保书问答助手。只能依据给定的事实与来源条款回答，不得超出证据推断。")
                .messages(Collections.singletonList(ModelMessage.builder()
                        .role("user")
                        .content(prompt.toString())
                        .build()))
                .temperature(0.1D)
                .maxTokens(700)
                .build();
    }

    private boolean answerNonFinal(QcQualityCertDataVO cert, QcJudgmentResultVO judgment) {
        if (JudgmentType.CAN_CONCESSION.getCode().equals(judgment.getJudgmentType())
                && (cert == null || !"SUCCESS".equals(cert.getStatus()))) {
            return true;
        }
        return cert == null;
    }

    private void persistCertQaAssessment(CertQaQueryCmd cmd,
                                         QcJudgmentResultVO judgment,
                                         CertQaAnswerVO answer,
                                         boolean aiGenerated) {
        try {
            AiAssessmentCreateCmd createCmd = new AiAssessmentCreateCmd();
            createCmd.setAssessmentType(ASSESSMENT_TYPE_CERT_QA);
            createCmd.setBusinessType(StringUtils.hasText(cmd.getCoilNo()) ? "COIL" : "BATCH");
            createCmd.setBusinessId(StringUtils.hasText(cmd.getCoilNo()) ? cmd.getCoilNo() : cmd.getBatchNo());
            createCmd.setRelatedJudgmentId(judgment.getJudgmentId());
            createCmd.setInputSnapshot(objectMapper.writeValueAsString(cmd));
            createCmd.setReferencesJson(objectMapper.writeValueAsString(answer.getCitations()));
            createCmd.setModelProvider(aiGenerated ? modelGateway.provider() : null);
            createCmd.setPromptVersion(PROMPT_VERSION);
            createCmd.setRawOutput(answer.getAnswer());
            createCmd.setStructuredOutput(objectMapper.writeValueAsString(answer));
            createCmd.setConfidenceLabel(answer.getConfidenceLabel());
            createCmd.setConfidenceScore("HIGH".equals(answer.getConfidenceLabel())
                    ? BigDecimal.valueOf(0.85D)
                    : BigDecimal.valueOf(0.65D));
            createCmd.setDegradationSource(answer.getDegradationSource());
            createCmd.setCacheHit(0);
            aiAssessmentService.create(createCmd);
        } catch (Exception e) {
            log.warn("保存质保书问答AI评估失败，coilNo={}, batchNo={}, error={}",
                    cmd.getCoilNo(), cmd.getBatchNo(), e.getMessage());
        }
    }

    private CertQaAnswerVO tryCache(CertQaQueryCmd cmd,
                                    List<AiSourceReferenceVO> citations,
                                    List<QcJudgmentResultVO.EvidenceVO> basis) {
        QcAiCache cache = null;
        if (StringUtils.hasText(cmd.getCoilNo())) {
            cache = aiCacheService.findActive("CERT_QA", "COIL", cmd.getCoilNo());
        }
        if (cache == null && StringUtils.hasText(cmd.getBatchNo())) {
            cache = aiCacheService.findActive("CERT_QA", "BATCH", cmd.getBatchNo());
        }
        if (cache == null) {
            return null;
        }
        String answerText = parseCacheAnswer(cache.getCachedOutput());
        String trusted = CitationReferenceSupport.acceptTrustedCitedOutput(answerText, citations, basis);
        if (!StringUtils.hasText(trusted)) {
            log.warn("质保书问答缓存引用校验未通过，coilNo={}, batchNo={}",
                    cmd.getCoilNo(), cmd.getBatchNo());
            return null;
        }
        CertQaAnswerVO answer = initAnswer();
        answer.setCacheHit(true);
        answer.setConfidenceLabel(cache.getConfidenceLabel());
        answer.setDegradationSource("CACHE");
        answer.setCitations(citations);
        answer.setIndicatorBasis(basis);
        answer.setAnswer(trusted);
        answer.setNonFinal(trusted.contains("不能生成正式质保书") || trusted.contains("预览"));
        return answer;
    }

    private String parseCacheAnswer(String cachedOutput) {
        try {
            Map<String, Object> map = objectMapper.readValue(cachedOutput, new TypeReference<Map<String, Object>>() {
            });
            Object answer = map.get("answer");
            Object summary = map.get("summary");
            return answer != null ? answer.toString() : (summary == null ? cachedOutput : summary.toString());
        } catch (Exception e) {
            return cachedOutput;
        }
    }

    private CertQaAnswerVO initAnswer() {
        CertQaAnswerVO answer = new CertQaAnswerVO();
        answer.setRefused(false);
        answer.setNonFinal(false);
        answer.setCertificateSnapshotFound(false);
        answer.setCacheHit(false);
        answer.setCitations(new ArrayList<>());
        answer.setIndicatorBasis(new ArrayList<>());
        answer.setConfidenceLabel("MEDIUM");
        answer.setDegradationSource("RULE_TEMPLATE");
        return answer;
    }

    private QcQualityCertDataVO loadLatestCert(CertQaQueryCmd cmd) {
        IPage<QcQualityCertDataVO> page = certDataService.page(1, 1, cmd.getCoilNo(), cmd.getBatchNo(), null, null);
        return page.getRecords().isEmpty() ? null : page.getRecords().get(0);
    }

    private QcJudgmentResultVO loadFinalJudgment(CertQaQueryCmd cmd) {
        QcJudgmentPageQuery query = new QcJudgmentPageQuery();
        query.setCoilNo(cmd.getCoilNo());
        query.setBatchNo(cmd.getBatchNo());
        query.setIsFinal(1);
        query.setPageNum(1);
        query.setPageSize(1);
        IPage<QcJudgmentListVO> page = judgmentService.page(query);
        if (page.getRecords().isEmpty()) {
            return null;
        }
        return judgmentService.getExplanationSnapshot(page.getRecords().get(0).getId());
    }

    private void applyStateGate(CertQaAnswerVO answer, QcQualityCertDataVO cert, QcJudgmentResultVO judgment) {
        String type = judgment.getJudgmentType();
        if (JudgmentType.STANDARD_CONFLICT.getCode().equals(type)) {
            answer.setNonFinal(true);
            refuse(answer, "当前最终判定为标准冲突，完成冲突裁决前不能形成正式质保书结论");
            return;
        }
        if (JudgmentType.UNQUALIFIED.getCode().equals(type) || JudgmentType.NEED_REINSPECTION.getCode().equals(type)) {
            answer.setNonFinal(true);
            refuse(answer, "当前判定为 " + type + "，不满足正式质保书放行条件");
            return;
        }
        if (JudgmentType.CAN_CONCESSION.getCode().equals(type)
                && (cert == null || !"SUCCESS".equals(cert.getStatus()))) {
            answer.setNonFinal(true);
            answer.setAnswer("当前为可让步判定，但尚未确认存在正式质保书快照；需完成客户确认和让步审批后才能正式化。");
        }
        if (cert == null) {
            answer.setNonFinal(true);
        }
    }

    private List<QcJudgmentResultVO.EvidenceVO> resolveIndicatorBasis(String question, QcJudgmentResultVO judgment) {
        if (judgment.getEvidences() == null) {
            return Collections.emptyList();
        }
        List<QcJudgmentResultVO.EvidenceVO> matched = judgment.getEvidences().stream()
                .filter(e -> contains(question, e.getIndicatorName()) || contains(question, e.getIndicatorCode()))
                .collect(Collectors.toList());
        return matched.isEmpty() ? judgment.getEvidences() : matched;
    }

    private String buildAnswerText(String question, QcQualityCertDataVO cert, QcJudgmentResultVO judgment,
                                   List<QcJudgmentResultVO.EvidenceVO> basis) {
        StringBuilder sb = new StringBuilder();
        if (cert == null) {
            sb.append("未找到正式质保书快照，以下回答来自检验记录和最终判定快照。");
        } else {
            sb.append("已找到质保书快照，状态=").append(cert.getStatus()).append("。");
        }
        sb.append("当前最终判定为 ").append(judgment.getJudgmentType()).append("。");
        if (!basis.isEmpty()) {
            sb.append("关键指标依据：");
            for (QcJudgmentResultVO.EvidenceVO e : basis) {
                sb.append(nullToEmpty(e.getIndicatorName()))
                        .append(" 实测=").append(e.getTestValue())
                        .append(" 下限=").append(e.getLowerLimit())
                        .append(" 上限=").append(e.getUpperLimit())
                        .append(" 偏差=").append(e.getDeviation())
                        .append(" 规则=").append(nullToEmpty(e.getTriggerRule()))
                        .append("；");
            }
        }
        return sb.toString();
    }

    private CertQaAnswerVO refuse(CertQaAnswerVO answer, String reason) {
        answer.setRefused(true);
        answer.setRefusalReason(reason);
        answer.setAnswer(reason);
        answer.setConfidenceLabel("LOW");
        answer.setDegradationSource("RULE_TEMPLATE");
        return answer;
    }

    private boolean contains(String question, String keyword) {
        return StringUtils.hasText(question) && StringUtils.hasText(keyword) && question.contains(keyword);
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
