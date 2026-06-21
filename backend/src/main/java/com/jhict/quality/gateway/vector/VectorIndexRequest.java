package com.jhict.quality.gateway.vector;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Provider-neutral clause indexing request.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VectorIndexRequest {

    private String traceId;

    private String businessType;

    private String businessId;

    private String indexName;

    private List<VectorClauseDocument> clauses;

    private Map<String, Object> metadata;
}
