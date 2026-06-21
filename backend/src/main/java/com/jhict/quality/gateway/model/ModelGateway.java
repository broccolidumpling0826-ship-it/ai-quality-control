package com.jhict.quality.gateway.model;

/**
 * Provider-neutral model gateway.
 *
 * <p>Business services must depend on this interface instead of DeepSeek or any
 * other provider-specific HTTP contract.</p>
 */
public interface ModelGateway {

    /**
     * Generate a chat/completion response from supplied facts, instructions, and citations.
     *
     * @param request provider-neutral chat request
     * @return provider-neutral chat response
     */
    ModelChatResponse chat(ModelChatRequest request);

    /**
     * Generate embeddings for retrieval/indexing text.
     *
     * @param request provider-neutral embedding request
     * @return provider-neutral embedding response
     */
    ModelEmbeddingResponse embed(ModelEmbeddingRequest request);

    /**
     * @return provider name for logs and assessment records
     */
    String provider();

    /**
     * @return whether this gateway is configured for external calls
     */
    boolean enabled();
}
