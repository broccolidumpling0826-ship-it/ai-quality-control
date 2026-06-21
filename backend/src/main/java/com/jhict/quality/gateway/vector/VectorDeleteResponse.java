package com.jhict.quality.gateway.vector;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Provider-neutral clause delete response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VectorDeleteResponse {

    private boolean success;

    private String traceId;

    private String provider;

    private String indexName;

    private String clauseId;

    private Long latencyMillis;

    private String errorCategory;

    private String errorMessage;

    private Map<String, Object> metadata;
}
