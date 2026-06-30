package com.jhict.quality.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.ai.degradation.cache")
public class AiDegradationCacheProperties {

    private boolean enabled = true;

    private String mode = "FALLBACK_ONLY";

    private Integer ttlDays = 30;

    private boolean writeOnValidatedGenerated = true;

    public boolean isFallbackOnly() {
        return "FALLBACK_ONLY".equalsIgnoreCase(mode);
    }

    public int effectiveTtlDays() {
        return ttlDays == null || ttlDays <= 0 ? 30 : ttlDays;
    }
}
