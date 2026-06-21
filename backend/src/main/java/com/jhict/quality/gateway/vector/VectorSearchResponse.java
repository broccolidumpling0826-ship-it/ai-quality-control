package com.jhict.quality.gateway.vector;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Provider-neutral standard clause retrieval response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VectorSearchResponse {

    private boolean success;

    private String traceId;

    private String provider;

    private String indexName;

    private List<VectorSearchResult> results;

    private Long latencyMillis;

    private String errorCategory;

    private String errorMessage;

    private Map<String, Object> metadata;
}
