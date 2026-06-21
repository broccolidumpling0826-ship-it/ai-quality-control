package com.jhict.quality.gateway.model.deepseek;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Environment-backed configuration for the DeepSeek-compatible model gateway.
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.ai.model")
public class DeepSeekModelProperties {

    private boolean enabled = false;

    private String baseUrl = "https://api.deepseek.com";

    private String apiKey;

    private String chatModel = "deepseek-v4-flash";

    private String embeddingModel = "text-embedding-v1";

    private boolean thinkingEnabled = false;

    private String reasoningEffort = "high";

    private Integer timeoutMillis = 15000;
}
