package com.jhict.quality.service.impl;

import com.jhict.quality.entity.QcAiCache;
import com.jhict.quality.mapper.QcAiCacheMapper;
import com.jhict.quality.service.support.ai.AiFallbackCacheContext;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AiCacheServiceImplTest {

    @Test
    void saveValidatedGeneratedUsesCompactCacheKey() {
        QcAiCacheMapper aiCacheMapper = mock(QcAiCacheMapper.class);
        when(aiCacheMapper.selectList(any())).thenReturn(Collections.emptyList());
        AiCacheServiceImpl service = new AiCacheServiceImpl();
        ReflectionTestUtils.setField(service, "aiCacheMapper", aiCacheMapper);

        AiFallbackCacheContext context = AiFallbackCacheContext.builder()
                .assessmentType("STANDARD_RAG")
                .businessType("STANDARD_QUERY")
                .businessId(repeat("很长的问题内容", 30))
                .promptVersion("standard-rag-v1")
                .outputText("合格输出")
                .confidenceLabel("HIGH")
                .build();

        service.saveValidatedGenerated(context, "abcdef1234567890", 30);

        ArgumentCaptor<QcAiCache> cacheCaptor = ArgumentCaptor.forClass(QcAiCache.class);
        verify(aiCacheMapper).insert(cacheCaptor.capture());
        assertThat(cacheCaptor.getValue().getCacheKey()).isEqualTo("STANDARD_RAG:STANDARD_QUERY:abcdef1234567890");
        assertThat(cacheCaptor.getValue().getCacheKey()).hasSizeLessThan(160);
    }

    private String repeat(String value, int times) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < times; i++) {
            builder.append(value);
        }
        return builder.toString();
    }
}
