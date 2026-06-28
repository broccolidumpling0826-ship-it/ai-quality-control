package com.jhict.quality.service.support.prompt;

import com.jhict.quality.gateway.model.ModelChatRequest;
import com.jhict.quality.gateway.model.ModelMessage;
import com.jhict.quality.vo.ConcessionRiskAssessmentVO;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component
public class ConcessionRiskPromptBuilder {

    private final PromptRegistry promptRegistry;

    public ConcessionRiskPromptBuilder(PromptRegistry promptRegistry) {
        this.promptRegistry = promptRegistry;
    }

    public ModelChatRequest build(String businessId, ConcessionRiskAssessmentVO result) {
        PromptTemplate template = promptRegistry.resolve(PromptScene.CONCESSION_RISK);
        Map<String, String> values = new HashMap<>();
        values.put("narrativeExplanation", result.getNarrativeExplanation());
        values.put("riskLevel", result.getRiskLevel());
        values.put("suggestedConditions", String.valueOf(result.getSuggestedConditions()));
        String userPrompt = trimTrailingNewlines(
                PromptPlaceholderUtils.apply(template.getUserSkeleton(), values));

        return ModelChatRequest.builder()
                .businessType(template.getBusinessType())
                .businessId(businessId)
                .promptVersion(template.getVersion())
                .systemPrompt(template.getSystemPrompt())
                .messages(Collections.singletonList(ModelMessage.builder()
                        .role("user")
                        .content(userPrompt)
                        .build()))
                .temperature(template.getTemperature())
                .maxTokens(template.getMaxTokens())
                .build();
    }

    public String activePromptVersion() {
        return promptRegistry.activeVersion(PromptScene.CONCESSION_RISK);
    }

    private static String trimTrailingNewlines(String value) {
        if (value == null || value.isEmpty()) {
            return value == null ? "" : value;
        }
        int end = value.length();
        while (end > 0 && (value.charAt(end - 1) == '\n' || value.charAt(end - 1) == '\r')) {
            end--;
        }
        return value.substring(0, end);
    }
}
