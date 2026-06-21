package com.jhict.quality.gateway.vector;

/**
 * Provider-neutral vector store gateway for standard clauses.
 */
public interface VectorStoreGateway {

    /**
     * Index standard/agreement/case clauses into the backing vector store.
     *
     * @param request indexing request
     * @return indexing response
     */
    VectorIndexResponse indexClauses(VectorIndexRequest request);

    /**
     * Search indexed clauses using a natural-language query and structured filters.
     *
     * @param request retrieval request
     * @return retrieval response
     */
    VectorSearchResponse searchClauses(VectorSearchRequest request);

    /**
     * Delete a clause document from the backing vector store.
     *
     * @param request delete request
     * @return delete response
     */
    VectorDeleteResponse deleteClause(VectorDeleteRequest request);

    /**
     * @return provider name for logs and assessment records
     */
    String provider();

    /**
     * @return whether the vector store is configured for external calls
     */
    boolean enabled();
}
