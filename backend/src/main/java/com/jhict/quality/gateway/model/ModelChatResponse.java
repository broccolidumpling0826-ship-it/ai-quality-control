package com.jhict.quality.gateway.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Provider-neutral chat response returned by model gateway implementations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelChatResponse {

    private boolean success;

    private String traceId;

    private String provider;

    private String modelName;

    private String content;

    private String finishReason;

    private Integer promptTokens;

    private Integer completionTokens;

    private Integer totalTokens;

    private Long latencyMillis;

    private String errorCategory;

    private String errorMessage;

    /**
     * Sanitized raw provider response or compact diagnostic data. Must not contain secrets.
     */
    private String rawResponse;

    private Map<String, Object> metadata;
}
