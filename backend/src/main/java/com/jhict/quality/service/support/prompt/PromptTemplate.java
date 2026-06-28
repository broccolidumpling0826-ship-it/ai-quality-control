package com.jhict.quality.service.support.prompt;

import com.jhict.quality.service.support.rag.CitationReferenceSupport.CitationPromptStyle;
import lombok.Builder;
import lombok.Value;

import java.util.Collections;
import java.util.Map;

@Value
@Builder
public class PromptTemplate {

    PromptScene scene;
    String version;
    String businessType;
    String systemPrompt;
    String userSkeleton;
    String userPrompt;
    CitationPromptStyle citationStyle;
    Double temperature;
    Integer maxTokens;
    @Builder.Default
    Map<String, String> conditionalSnippets = Collections.emptyMap();
}
