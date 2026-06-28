package com.jhict.quality.service.support.prompt;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PromptRenderResult {

    String systemPrompt;
    String userPrompt;
    String promptVersion;
    String businessType;
    Double temperature;
    Integer maxTokens;
}
