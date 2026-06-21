package com.jhict.quality.ai.client;

import com.jhict.quality.enums.AiErrorType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LlmResponse {

    private boolean success;

    private boolean degraded;

    private String content;

    private AiErrorType errorType;

    private String errorMessage;

    private int promptTokens;

    private int completionTokens;

    private int totalTokens;

    private long latencyMs;

    private String auditLogId;
}
