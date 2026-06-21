package com.jhict.quality.gateway.vector;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Provider-neutral clause indexing response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VectorIndexResponse {

    private boolean success;

    private String traceId;

    private String provider;

    private String indexName;

    private Integer indexedCount;

    private List<String> failedClauseIds;

    private Long latencyMillis;

    private String errorCategory;

    private String errorMessage;

    private Map<String, Object> metadata;
}
