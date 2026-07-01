package com.jhict.quality.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jhict.quality.dto.AiAssessmentCreateCmd;
import com.jhict.quality.dto.CertQaQueryCmd;
import com.jhict.quality.dto.QcJudgmentPageQuery;
import com.jhict.quality.entity.QcAiCache;
import com.jhict.quality.entity.QcConcessionAcceptance;
import com.jhict.quality.mapper.QcConcessionAcceptanceMapper;
import com.jhict.quality.enums.JudgmentType;
import com.jhict.quality.gateway.model.ModelChatRequest;
import com.jhict.quality.gateway.model.ModelChatResponse;
import com.jhict.quality.gateway.model.ModelGateway;
import com.jhict.quality.gateway.model.ModelMessage;
import com.jhict.quality.service.api.AiAssessmentService;
import com.jhict.quality.service.api.CertDataService;
import com.jhict.quality.service.api.CertQaService;
import com.jhict.quality.service.api.JudgmentService;
import com.jhict.quality.service.support.ai.AiFallbackCacheContext;
import com.jhict.quality.service.support.ai.AiFallbackCacheService;
import com.jhict.quality.service.support.prompt.CertQaPromptBuilder;
import com.jhict.quality.service.support.rag.CitationReferenceSupport;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CertQaServiceImpl implements CertQaService {

    private static final String ASSESSMENT_TYPE_CERT_QA = "CERT_QA";
    private static final String MSG_CONCESSION_PENDING_WORKFLOW =
            "当前为可让步判定，尚未完成客户确认或内部让步审批，暂不能生成正式质保书。"
                    + "请先在【质量流程 → 让步接收】完成客户确认与审批。";
    private static final String MSG_CONCESSION_APPROVED_NEED_SNAPSHOT =
            "让步接收已完成（客户确认与内部审批已通过），当前仅缺正式质保书数据快照。"
                    + "请前往【数据汇总 → 质保书数据】，输入本卷卷号或批次号后点击「生成」；"
                    + "生成成功后可在此问答页确认出证依据，并导出正式 PDF。";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Resource
    private CertDataService certDataService;

    @Resource
    private JudgmentService judgmentService;

    @Resource
    private ModelGateway modelGateway;

    @Resource
    private AiAssessmentService aiAssessmentService;

    @Resource
    private QcConcessionAcceptanceMapper concessionMapper;

    @Resource
    private CertQaPromptBuilder certQaPromptBuilder;

    @Resource
    private AiFallbackCacheService aiFallbackCacheService;

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

        answer.setCitations(citations);
        answer.setIndicatorBasis(basis);
        boolean concessionApproved = isConcessionApproved(judgment.getJudgmentId());
        applyStateGate(answer, cert, judgment, concessionApproved);
        if (Boolean.TRUE.equals(answer.getRefused())) {
            return answer;
        }

        String ruleAnswer = buildAnswerText(cert, judgment, basis, concessionApproved);
        if (shouldUseGuidanceRuleOnly(answer, cert, judgment, concessionApproved)) {
            String guidedAnswer = StringUtils.hasText(answer.getGuidanceMessage())
                    ? answer.getGuidanceMessage() + "\n\n" + ruleAnswer
                    : ruleAnswer;
            answer.setAnswer(guidedAnswer);
            applyRuleDegradation(answer, citations, guidedAnswer);
            persistCertQaAssessment(cmd, judgment, answer, false);
            return answer;
        }

        fillGeneratedAnswer(cmd, answer, cert, judgment, ruleAnswer, concessionApproved);
        return answer;
    }

    private void fillGeneratedAnswer(CertQaQueryCmd cmd,
                                     CertQaAnswerVO answer,
                                     QcQualityCertDataVO cert,
                                     QcJudgmentResultVO judgment,
                                     String ruleAnswer,
                                     boolean concessionApproved) {
        if (StringUtils.hasText(answer.getGuidanceMessage())) {
            ruleAnswer = answer.getGuidanceMessage() + "\n\n" + ruleAnswer;
        }
        answer.setAnswer(ruleAnswer);
        List<AiSourceReferenceVO> citations = answer.getCitations() == null
                ? Collections.emptyList() : answer.getCitations();
        if (!modelGateway.enabled()) {
            if (applyFallbackCache(cmd, judgment, answer)) {
                persistCertQaAssessment(cmd, judgment, answer, false);
                return;
            }
            applyRuleDegradation(answer, citations, ruleAnswer);
            return;
        }
        ModelChatResponse response = modelGateway.chat(buildCertQaChatRequest(
                cmd, cert, judgment, answer.getIndicatorBasis(), citations, ruleAnswer, concessionApproved));
        String trusted = CitationReferenceSupport.acceptTrustedCitedOutput(
                response != null ? response.getContent() : null, citations, answer.getIndicatorBasis());
        if (StringUtils.hasText(trusted)) {
            answer.setAnswer(trusted);
            answer.setConfidenceLabel(citations.isEmpty() ? "MEDIUM" : "HIGH");
            answer.setDegradationSource("GENERATED");
            answer.setDegradationReason("AI回答已基于判定快照与来源条款生成并通过引用校验");
            aiFallbackCacheService.saveValidatedGenerated(buildCertQaCacheContext(cmd, judgment, answer, trusted));
            persistCertQaAssessment(cmd, judgment, answer, true);
            return;
        }
        if (response != null && response.isSuccess()) {
            log.warn("质保书问答引用校验未通过，coilNo={}, batchNo={}", cmd.getCoilNo(), cmd.getBatchNo());
        } else if (response != null) {
            log.warn("质保书问答生成失败，coilNo={}, batchNo={}, error={}",
                    cmd.getCoilNo(), cmd.getBatchNo(), response.getErrorMessage());
        }
        if (applyFallbackCache(cmd, judgment, answer)) {
            persistCertQaAssessment(cmd, judgment, answer, false);
            return;
        }
        applyRuleDegradation(answer, citations, ruleAnswer);
        persistCertQaAssessment(cmd, judgment, answer, false);
    }

    private boolean applyFallbackCache(CertQaQueryCmd cmd,
                                       QcJudgmentResultVO judgment,
                                       CertQaAnswerVO answer) {
        QcAiCache fallbackCache = aiFallbackCacheService.findFallbackCache(
                buildCertQaCacheContext(cmd, judgment, answer, null));
        if (fallbackCache == null || !StringUtils.hasText(fallbackCache.getCachedOutput())) {
            return false;
        }
        answer.setAnswer(parseCacheAnswer(fallbackCache.getCachedOutput()));
        answer.setConfidenceLabel(fallbackCache.getConfidenceLabel());
        answer.setDegradationSource("CACHE");
        answer.setDegradationReason("引用校验未通过或模型未启用，已使用同输入快照缓存回答");
        answer.setCacheHit(true);
        answer.setNonFinal(answer.getAnswer() != null
                && (answer.getAnswer().contains("不能生成正式质保书") || answer.getAnswer().contains("预览")));
        return true;
    }

    private void applyRuleDegradation(CertQaAnswerVO answer,
                                      List<AiSourceReferenceVO> citations,
                                      String ruleAnswer) {
        answer.setAnswer(ruleAnswer);
        if (citations.isEmpty()) {
            answer.setConfidenceLabel("MEDIUM");
            answer.setDegradationSource("RULE_TEMPLATE");
            answer.setDegradationReason("未命中可引用来源段落，以上基于结构化判定快照");
            answer.setAnswer(ruleAnswer + " 未找到可引用的来源段落，以上仅基于结构化判定快照。");
        } else {
            answer.setConfidenceLabel("MEDIUM");
            answer.setDegradationSource("RAW_RETRIEVAL");
            answer.setDegradationReason("引用校验未通过或模型调用失败，已展示结构化判定快照");
        }
    }

    private ModelChatRequest buildCertQaChatRequest(CertQaQueryCmd cmd,
                                                    QcQualityCertDataVO cert,
                                                    QcJudgmentResultVO judgment,
                                                    List<QcJudgmentResultVO.EvidenceVO> basis,
                                                    List<AiSourceReferenceVO> citations,
                                                    String ruleAnswer,
                                                    boolean concessionApproved) {
        boolean nonFinal = answerNonFinal(cert, judgment);
        return certQaPromptBuilder.build(cmd, cert, judgment, basis, citations, ruleAnswer,
                concessionApproved, nonFinal, needsCertSnapshot(cert, judgment));
    }

    private boolean answerNonFinal(QcQualityCertDataVO cert, QcJudgmentResultVO judgment) {
        return needsCertSnapshot(cert, judgment) || cert == null;
    }

    private boolean needsCertSnapshot(QcQualityCertDataVO cert, QcJudgmentResultVO judgment) {
        return JudgmentType.CAN_CONCESSION.getCode().equals(judgment.getJudgmentType())
                && (cert == null || !"SUCCESS".equals(cert.getStatus()));
    }

    private boolean shouldUseGuidanceRuleOnly(CertQaAnswerVO answer,
                                              QcQualityCertDataVO cert,
                                              QcJudgmentResultVO judgment,
                                              boolean concessionApproved) {
        return StringUtils.hasText(answer.getGuidanceMessage())
                && concessionApproved
                && needsCertSnapshot(cert, judgment);
    }

    private boolean isConcessionApproved(String judgmentId) {
        if (!StringUtils.hasText(judgmentId)) {
            return false;
        }
        Long count = concessionMapper.selectCount(new LambdaQueryWrapper<QcConcessionAcceptance>()
                .eq(QcConcessionAcceptance::getJudgmentId, judgmentId)
                .eq(QcConcessionAcceptance::getApprovalStatus, "APPROVED")
                .and(w -> w.isNull(QcConcessionAcceptance::getExpiryDate)
                        .or()
                        .ge(QcConcessionAcceptance::getExpiryDate, LocalDate.now())));
        return count != null && count > 0;
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
            createCmd.setPromptVersion(certQaPromptBuilder.activePromptVersion());
            createCmd.setRawOutput(answer.getAnswer());
            createCmd.setStructuredOutput(objectMapper.writeValueAsString(answer));
            createCmd.setConfidenceLabel(answer.getConfidenceLabel());
            createCmd.setConfidenceScore("HIGH".equals(answer.getConfidenceLabel())
                    ? BigDecimal.valueOf(0.85D)
                    : BigDecimal.valueOf(0.65D));
            createCmd.setDegradationSource(answer.getDegradationSource());
            createCmd.setCacheHit(Boolean.TRUE.equals(answer.getCacheHit()) ? 1 : 0);
            aiAssessmentService.create(createCmd);
        } catch (Exception e) {
            log.warn("保存质保书问答AI评估失败，coilNo={}, batchNo={}, error={}",
                    cmd.getCoilNo(), cmd.getBatchNo(), e.getMessage());
        }
    }

    private AiFallbackCacheContext buildCertQaCacheContext(CertQaQueryCmd cmd,
                                                           QcJudgmentResultVO judgment,
                                                           CertQaAnswerVO answer,
                                                           String outputText) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("question", cmd.getQuestion());
        snapshot.put("coilNo", cmd.getCoilNo());
        snapshot.put("batchNo", cmd.getBatchNo());
        snapshot.put("judgmentId", judgment.getJudgmentId());
        snapshot.put("judgmentType", judgment.getJudgmentType());
        snapshot.put("indicatorBasis", answer.getIndicatorBasis());
        snapshot.put("citations", answer.getCitations());
        return AiFallbackCacheContext.builder()
                .assessmentType(ASSESSMENT_TYPE_CERT_QA)
                .businessType(StringUtils.hasText(cmd.getCoilNo()) ? "COIL" : "BATCH")
                .businessId(StringUtils.hasText(cmd.getCoilNo()) ? cmd.getCoilNo() : cmd.getBatchNo())
                .promptVersion(certQaPromptBuilder.activePromptVersion())
                .modelName(modelGateway.provider())
                .inputSnapshot(snapshot)
                .outputText(outputText)
                .structuredOutput(toJsonQuietly(answer.getCitations()))
                .references(answer.getCitations())
                .confidenceLabel(answer.getConfidenceLabel())
                .confidenceScore("HIGH".equals(answer.getConfidenceLabel())
                        ? BigDecimal.valueOf(0.85D) : BigDecimal.valueOf(0.65D))
                .build();
    }

    private String toJsonQuietly(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            return null;
        }
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

    private void applyStateGate(CertQaAnswerVO answer,
                                QcQualityCertDataVO cert,
                                QcJudgmentResultVO judgment,
                                boolean concessionApproved) {
        String type = judgment.getJudgmentType();
        answer.setConcessionApproved(concessionApproved);
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
        if (needsCertSnapshot(cert, judgment)) {
            answer.setNonFinal(true);
            if (concessionApproved) {
                answer.setGuidanceMessage(MSG_CONCESSION_APPROVED_NEED_SNAPSHOT);
            } else {
                answer.setGuidanceMessage(MSG_CONCESSION_PENDING_WORKFLOW);
            }
        } else if (cert == null) {
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

    private String buildAnswerText(QcQualityCertDataVO cert,
                                   QcJudgmentResultVO judgment,
                                   List<QcJudgmentResultVO.EvidenceVO> basis,
                                   boolean concessionApproved) {
        StringBuilder sb = new StringBuilder();
        if (cert == null) {
            sb.append("未找到正式质保书快照，以下回答来自检验记录和最终判定快照。");
        } else {
            sb.append("已找到质保书快照，状态=").append(cert.getStatus()).append("。");
        }
        sb.append("当前最终判定为 ").append(judgment.getJudgmentType()).append("。");
        if (JudgmentType.CAN_CONCESSION.getCode().equals(judgment.getJudgmentType())) {
            sb.append("让步接收审批状态：").append(concessionApproved ? "已通过" : "未完成").append("。");
        }
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
        answer.setDegradationSource("UNAVAILABLE");
        answer.setDegradationReason(reason);
        return answer;
    }

    private boolean contains(String question, String keyword) {
        return StringUtils.hasText(question) && StringUtils.hasText(keyword) && question.contains(keyword);
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
