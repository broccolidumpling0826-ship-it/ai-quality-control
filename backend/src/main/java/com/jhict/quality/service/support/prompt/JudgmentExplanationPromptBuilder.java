package com.jhict.quality.service.support.prompt;

import com.jhict.quality.entity.QcJudgmentResult;
import com.jhict.quality.gateway.model.ModelChatRequest;
import com.jhict.quality.gateway.model.ModelMessage;
import com.jhict.quality.service.support.rag.CitationReferenceSupport;
import com.jhict.quality.vo.AiSourceReferenceVO;
import com.jhict.quality.vo.QcJudgmentResultVO;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class JudgmentExplanationPromptBuilder {

    private final PromptRegistry promptRegistry;

    public JudgmentExplanationPromptBuilder(PromptRegistry promptRegistry) {
        this.promptRegistry = promptRegistry;
    }

    public ModelChatRequest build(QcJudgmentResultVO vo,
                                  QcJudgmentResult result,
                                  List<AiSourceReferenceVO> citations) {
        PromptTemplate template = promptRegistry.resolve(PromptScene.JUDGMENT_EXPLANATION);
        Map<String, String> values = new HashMap<>();
        values.put("judgmentType", result.getJudgmentType());
        values.put("ruleExplanation", vo.getRuleExplanation());

        StringBuilder userPrompt = new StringBuilder(PromptPlaceholderUtils.apply(template.getUserSkeleton(), values));
        PromptEvidenceFormatter.appendEvidences(userPrompt, vo.getEvidences());
        CitationReferenceSupport.appendNumberedCitationBlock(userPrompt, citations);
        CitationReferenceSupport.appendCitationAnswerRules(userPrompt, template.getCitationStyle());

        return ModelChatRequest.builder()
                .businessType(template.getBusinessType())
                .businessId(result.getId())
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
        return promptRegistry.activeVersion(PromptScene.JUDGMENT_EXPLANATION);
    }
}
