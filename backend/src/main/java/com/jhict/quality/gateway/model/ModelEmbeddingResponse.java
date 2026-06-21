package com.jhict.quality.gateway.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Provider-neutral embedding response returned by model gateway implementations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelEmbeddingResponse {

    private boolean success;

    private String traceId;

    private String provider;

    private String modelName;

    private List<ModelEmbedding> embeddings;

    private Long latencyMillis;

    private String errorCategory;

    private String errorMessage;

    /**
     * Sanitized raw provider response or compact diagnostic data. Must not contain secrets.
     */
    private String rawResponse;

    private Map<String, Object> metadata;
}
