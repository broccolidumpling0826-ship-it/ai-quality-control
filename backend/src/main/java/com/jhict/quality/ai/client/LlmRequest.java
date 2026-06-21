package com.jhict.quality.ai.client;

import com.jhict.quality.enums.AiCallSource;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LlmRequest {

    private AiCallSource callSource;

    private String promptKey;

    private String systemPrompt;

    private String userPrompt;

    private String bizRefId;

    private String citationIdsJson;
}
