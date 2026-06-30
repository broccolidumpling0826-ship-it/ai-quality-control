package com.jhict.quality.service.support.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jhict.quality.config.AiDegradationCacheProperties;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

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

    @Test
    void buildInputHashIsStableForSameSnapshot() {
        AiFallbackCacheServiceImpl service = new AiFallbackCacheServiceImpl();
        ReflectionTestUtils.setField(service, "objectMapper", new ObjectMapper());

        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("judgmentType", "QUALIFIED");
        snapshot.put("evidences", Arrays.asList("Rm:430", "A:30.5"));

        AiFallbackCacheContext context = AiFallbackCacheContext.builder()
                .assessmentType("JUDGMENT_EXPLANATION")
                .businessType("QC_JUDGMENT_RESULT")
                .businessId("jud001")
                .promptVersion("prompt-v1")
                .modelName("deepseek-ai/DeepSeek-V4-Flash")
                .inputSnapshot(snapshot)
                .build();

        assertThat(service.buildInputHash(context)).isEqualTo(service.buildInputHash(context));
    }
}
