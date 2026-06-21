package com.jhict.quality.gateway.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Provider-neutral Vision OCR response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelVisionExtractionResponse {

    private boolean success;

    private String traceId;

    private String provider;

    private String modelName;

    private String extractedText;

    private String finishReason;

    private Integer promptTokens;

    private Integer completionTokens;

    private Integer totalTokens;

    private Long latencyMillis;

    private String errorCategory;

    private String errorMessage;

    private String rawResponse;

    private Map<String, Object> metadata;
}
