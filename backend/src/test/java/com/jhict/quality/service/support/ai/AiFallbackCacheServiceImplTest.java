package com.jhict.quality.service.support.ai;

import com.jhict.quality.config.AiDegradationCacheProperties;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AiFallbackCacheServiceImplTest {

    @Test
    void propertiesDefaultToFallbackOnlyWithThirtyDayTtl() {
        AiDegradationCacheProperties properties = new AiDegradationCacheProperties();

        assertThat(properties.isEnabled()).isTrue();
        assertThat(properties.isFallbackOnly()).isTrue();
        assertThat(properties.effectiveTtlDays()).isEqualTo(30);
        assertThat(properties.isWriteOnValidatedGenerated()).isTrue();
    }
}
