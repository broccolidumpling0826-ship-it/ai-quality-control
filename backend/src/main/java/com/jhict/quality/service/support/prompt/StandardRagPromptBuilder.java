package com.jhict.quality.service.support.prompt;

import com.jhict.quality.dto.StandardRagQueryCmd;
import com.jhict.quality.gateway.model.ModelChatRequest;
import com.jhict.quality.gateway.model.ModelMessage;
import com.jhict.quality.service.support.rag.CitationReferenceSupport;
import com.jhict.quality.vo.StandardRagSourceVO;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class StandardRagPromptBuilder {

    private final PromptRegistry promptRegistry;

    public StandardRagPromptBuilder(PromptRegistry promptRegistry) {
        this.promptRegistry = promptRegistry;
    }

    public BuiltStandardRagPrompt build(StandardRagQueryCmd cmd, List<StandardRagSourceVO> sources) {
        PromptTemplate template = promptRegistry.resolve(PromptScene.STANDARD_RAG);
        Map<String, String> values = new HashMap<>();
        values.put("query", cmd.getQuery());

        StringBuilder userPrompt = new StringBuilder(PromptPlaceholderUtils.apply(template.getUserSkeleton(), values));
        CitationReferenceSupport.appendNumberedRagCitationBlock(userPrompt, sources);
        CitationReferenceSupport.appendCitationAnswerRules(userPrompt, template.getCitationStyle());

        ModelChatRequest request = ModelChatRequest.builder()
                .businessType(template.getBusinessType())
                .promptVersion(template.getVersion())
                .systemPrompt(template.getSystemPrompt())
                .messages(Collections.singletonList(ModelMessage.builder()
                        .role("user")
                        .content(userPrompt.toString())
                        .build()))
                .temperature(template.getTemperature())
                .maxTokens(template.getMaxTokens())
                .build();
        return new BuiltStandardRagPrompt(request, userPrompt.length());
    }

    public String activePromptVersion() {
        return promptRegistry.activeVersion(PromptScene.STANDARD_RAG);
    }

    public static final class BuiltStandardRagPrompt {
        private final ModelChatRequest request;
        private final int userPromptLength;

        public BuiltStandardRagPrompt(ModelChatRequest request, int userPromptLength) {
            this.request = request;
            this.userPromptLength = userPromptLength;
        }

        public ModelChatRequest getRequest() {
            return request;
        }

        public int getUserPromptLength() {
            return userPromptLength;
        }
    }
}
