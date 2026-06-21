package com.jhict.quality.gateway.model.openai;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Environment-backed configuration for an OpenAI-compatible chat model gateway.
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.ai.model")
public class OpenAiCompatibleModelProperties {

    private boolean enabled = false;

    private String provider = "SILICONFLOW";

    private String baseUrl = "https://api.siliconflow.cn/v1";

    private String apiKey;

    private String chatModel = "Pro/zai-org/GLM-4.7";

    private boolean thinkingEnabled = false;

    private String reasoningEffort = "high";

    private Integer timeoutMillis = 15000;
}
