package com.jhict.quality.gateway.vector;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Provider-neutral clause delete request.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VectorDeleteRequest {

    private String traceId;

    private String indexName;

    private String clauseId;

    private String esDocumentKey;

    private Map<String, Object> metadata;
}
