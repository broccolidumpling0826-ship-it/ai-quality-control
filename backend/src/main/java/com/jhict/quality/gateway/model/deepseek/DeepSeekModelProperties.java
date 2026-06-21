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

    private String baseUrl = "https://api.deepseek.com/v1";

    private String apiKey;

    private String chatModel = "deepseek-chat";

    private String embeddingModel = "text-embedding-v1";

    private Integer timeoutMillis = 15000;
}
