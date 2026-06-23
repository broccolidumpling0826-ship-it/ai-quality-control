package com.jhict.quality.gateway.vector;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Delete all clause documents for one source document from the vector store.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VectorDeleteByDocumentRequest {

    private String traceId;

    private String indexName;

    private String documentId;

    private Map<String, Object> metadata;
}
