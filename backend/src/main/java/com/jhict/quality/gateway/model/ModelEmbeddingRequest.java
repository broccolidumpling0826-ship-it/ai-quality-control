package com.jhict.quality.gateway.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Provider-neutral embedding request for standard clause indexing and retrieval.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelEmbeddingRequest {

    private String traceId;

    private String businessType;

    private String businessId;

    private String modelName;

    private List<String> inputTexts;

    private Integer timeoutMillis;

    private Map<String, Object> metadata;
}
