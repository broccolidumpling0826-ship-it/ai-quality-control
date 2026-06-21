package com.jhict.quality.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.ai")
public class AiProperties {

    private boolean enabled = true;

    private String baseUrl = "https://api.openai.com/v1";

    private String apiKey = "";

    private String model = "gpt-4o-mini";

    private int timeoutMs = 5000;

    private int maxRetriesOn429 = 1;

    private int circuitBreakerThreshold = 3;

    private long circuitBreakerOpenMs = 60000L;

    /** 测试用：TIMEOUT / RATE_LIMIT / UNAVAILABLE */
    private String mockFailure = "";
}
