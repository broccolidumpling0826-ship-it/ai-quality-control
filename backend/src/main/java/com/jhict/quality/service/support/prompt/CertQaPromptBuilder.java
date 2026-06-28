package com.jhict.quality.service.support.prompt;

import com.jhict.quality.dto.CertQaQueryCmd;
import com.jhict.quality.enums.JudgmentType;
import com.jhict.quality.gateway.model.ModelChatRequest;
import com.jhict.quality.gateway.model.ModelMessage;
import com.jhict.quality.service.support.rag.CitationReferenceSupport;
import com.jhict.quality.vo.AiSourceReferenceVO;
import com.jhict.quality.vo.QcJudgmentResultVO;
import com.jhict.quality.vo.QcQualityCertDataVO;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class CertQaPromptBuilder {

    private final PromptRegistry promptRegistry;

    public CertQaPromptBuilder(PromptRegistry promptRegistry) {
        this.promptRegistry = promptRegistry;
    }

    public ModelChatRequest build(CertQaQueryCmd cmd,
                                  QcQualityCertDataVO cert,
                                  QcJudgmentResultVO judgment,
                                  List<QcJudgmentResultVO.EvidenceVO> basis,
                                  List<AiSourceReferenceVO> citations,
                                  String ruleAnswer,
                                  boolean concessionApproved,
                                  boolean nonFinal,
                                  boolean needsCertSnapshotAppend) {
        PromptTemplate template = promptRegistry.resolve(PromptScene.CERT_QA);
        Map<String, String> values = new HashMap<>();
        values.put("question", cmd.getQuestion());
        values.put("ruleAnswer", ruleAnswer);
        values.put("certStatus", cert != null ? cert.getStatus() : "未生成");
        values.put("judgmentType", judgment.getJudgmentType());
        values.put("nonFinal", nonFinal ? "是" : "否");

        StringBuilder userPrompt = new StringBuilder(PromptPlaceholderUtils.apply(template.getUserSkeleton(), values));
        if (JudgmentType.CAN_CONCESSION.getCode().equals(judgment.getJudgmentType())) {
            int insertAt = userPrompt.indexOf("是否非最终态：");
            if (insertAt >= 0) {
                userPrompt.insert(insertAt, "让步接收审批：" + (concessionApproved ? "已通过" : "未完成") + "\n");
            }
        }
        PromptEvidenceFormatter.appendEvidences(userPrompt, basis);
        CitationReferenceSupport.appendNumberedCitationBlock(userPrompt, citations);
        CitationReferenceSupport.appendCitationAnswerRules(userPrompt, template.getCitationStyle());

        if (concessionApproved && needsCertSnapshotAppend) {
            userPrompt.append(template.getConditionalSnippets().get("append-concession-approved"));
        } else if (nonFinal) {
            userPrompt.append(template.getConditionalSnippets().get("append-non-final"));
        }

        return ModelChatRequest.builder()
                .businessType(template.getBusinessType())
                .businessId(judgment.getJudgmentId() == null ? null : String.valueOf(judgment.getJudgmentId()))
                .promptVersion(template.getVersion())
                .systemPrompt(template.getSystemPrompt())
                .messages(Collections.singletonList(ModelMessage.builder()
                        .role("user")
                        .content(userPrompt.toString())
                        .build()))
                .temperature(template.getTemperature())
                .maxTokens(template.getMaxTokens())
                .build();
    }

    public String activePromptVersion() {
        return promptRegistry.activeVersion(PromptScene.CERT_QA);
    }
}
