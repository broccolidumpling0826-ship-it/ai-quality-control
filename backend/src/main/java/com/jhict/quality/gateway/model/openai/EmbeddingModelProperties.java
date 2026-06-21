package com.jhict.quality.gateway.model.openai;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Environment-backed configuration for an OpenAI-compatible embedding service.
 *
 * <p>This is intentionally separate from chat configuration because chat models
 * and embedding models may use different credentials and vector dimensions even
 * when they are served by the same platform.</p>
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.ai.embedding")
public class EmbeddingModelProperties {

    private boolean enabled = false;

    private String baseUrl = "https://api.siliconflow.cn/v1";

    private String apiKey;

    private String model = "BAAI/bge-m3";

    private String provider = "SILICONFLOW";

    private Integer timeoutMillis = 15000;
}
