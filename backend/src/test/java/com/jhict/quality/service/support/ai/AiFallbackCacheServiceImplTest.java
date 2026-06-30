package com.jhict.quality.service.support.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jhict.quality.config.AiDegradationCacheProperties;
import com.jhict.quality.service.api.AiCacheService;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

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

    @Test
    void buildInputHashNormalizesNestedMapOrder() {
        AiFallbackCacheServiceImpl service = newService(mock(AiCacheService.class));

        Map<String, Object> nestedA = new LinkedHashMap<>();
        nestedA.put("max", "500");
        nestedA.put("min", "300");
        Map<String, Object> snapshotA = new LinkedHashMap<>();
        snapshotA.put("limits", nestedA);
        snapshotA.put("evidences", Arrays.asList("Rm:430", nestedA));

        Map<String, Object> nestedB = new LinkedHashMap<>();
        nestedB.put("min", "300");
        nestedB.put("max", "500");
        Map<String, Object> snapshotB = new LinkedHashMap<>();
        snapshotB.put("evidences", Arrays.asList("Rm:430", nestedB));
        snapshotB.put("limits", nestedB);

        AiFallbackCacheContext contextA = baseContext()
                .inputSnapshot(snapshotA)
                .build();
        AiFallbackCacheContext contextB = baseContext()
                .inputSnapshot(snapshotB)
                .build();

        assertThat(service.buildInputHash(contextA)).isEqualTo(service.buildInputHash(contextB));
    }

    @Test
    void findFallbackCacheSkipsCacheServiceWhenModelNameMissing() {
        AiCacheService aiCacheService = mock(AiCacheService.class);
        AiFallbackCacheServiceImpl service = newService(aiCacheService);

        service.findFallbackCache(baseContext()
                .modelName(null)
                .inputSnapshot(simpleSnapshot())
                .build());

        verifyNoInteractions(aiCacheService);
    }

    @Test
    void saveValidatedGeneratedSkipsCacheServiceWhenConfidenceLabelMissing() {
        AiCacheService aiCacheService = mock(AiCacheService.class);
        AiFallbackCacheServiceImpl service = newService(aiCacheService);

        service.saveValidatedGenerated(baseContext()
                .inputSnapshot(simpleSnapshot())
                .outputText("validated answer")
                .confidenceLabel(null)
                .build());

        verifyNoInteractions(aiCacheService);
    }

    @Test
    void saveValidatedGeneratedDelegatesWhenRequiredFieldsArePresent() {
        AiCacheService aiCacheService = mock(AiCacheService.class);
        AiFallbackCacheServiceImpl service = newService(aiCacheService);
        AiFallbackCacheContext context = baseContext()
                .inputSnapshot(simpleSnapshot())
                .outputText("validated answer")
                .confidenceLabel("HIGH")
                .build();

        service.saveValidatedGenerated(context);

        verify(aiCacheService).saveValidatedGenerated(eq(context), anyString(), eq(30));
    }

    private AiFallbackCacheServiceImpl newService(AiCacheService aiCacheService) {
        AiFallbackCacheServiceImpl service = new AiFallbackCacheServiceImpl();
        ReflectionTestUtils.setField(service, "objectMapper", new ObjectMapper());
        ReflectionTestUtils.setField(service, "properties", new AiDegradationCacheProperties());
        ReflectionTestUtils.setField(service, "aiCacheService", aiCacheService);
        return service;
    }

    private AiFallbackCacheContext.AiFallbackCacheContextBuilder baseContext() {
        return AiFallbackCacheContext.builder()
                .assessmentType("JUDGMENT_EXPLANATION")
                .businessType("QC_JUDGMENT_RESULT")
                .businessId("jud001")
                .promptVersion("prompt-v1")
                .modelName("deepseek-ai/DeepSeek-V4-Flash");
    }

    private Map<String, Object> simpleSnapshot() {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("judgmentType", "QUALIFIED");
        return snapshot;
    }
}
