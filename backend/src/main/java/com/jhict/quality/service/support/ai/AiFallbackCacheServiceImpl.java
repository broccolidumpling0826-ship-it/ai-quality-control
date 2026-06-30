package com.jhict.quality.service.support.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jhict.quality.config.AiDegradationCacheProperties;
import com.jhict.quality.entity.QcAiCache;
import com.jhict.quality.service.api.AiCacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Service
public class AiFallbackCacheServiceImpl implements AiFallbackCacheService {

    @Resource
    private AiDegradationCacheProperties properties;

    @Resource
    private AiCacheService aiCacheService;

    @Resource
    private ObjectMapper objectMapper;

    @Override
    public String buildInputHash(AiFallbackCacheContext context) {
        if (context == null || context.getInputSnapshot() == null || context.getInputSnapshot().isEmpty()) {
            return null;
        }
        Map<String, Object> stable = new LinkedHashMap<>();
        stable.put("assessmentType", context.getAssessmentType());
        stable.put("businessType", context.getBusinessType());
        stable.put("businessId", context.getBusinessId());
        stable.put("promptVersion", context.getPromptVersion());
        stable.put("modelName", context.getModelName());
        stable.put("inputSnapshot", context.getInputSnapshot());
        try {
            String json = objectMapper.writeValueAsString(stable);
            return DigestUtils.md5DigestAsHex(json.getBytes(StandardCharsets.UTF_8));
        } catch (JsonProcessingException e) {
            log.warn("构造AI缓存inputHash失败，assessmentType={}, businessId={}, error={}",
                    context.getAssessmentType(), context.getBusinessId(), e.getMessage());
            return null;
        }
    }

    @Override
    public QcAiCache findFallbackCache(AiFallbackCacheContext context) {
        if (!cacheReadable(context)) {
            return null;
        }
        String inputHash = buildInputHash(context);
        if (!StringUtils.hasText(inputHash)) {
            return null;
        }
        return aiCacheService.findActive(context.getAssessmentType(), context.getBusinessType(),
                context.getBusinessId(), context.getPromptVersion(), inputHash);
    }

    @Override
    public void saveValidatedGenerated(AiFallbackCacheContext context) {
        if (!cacheWritable(context)) {
            return;
        }
        String inputHash = buildInputHash(context);
        if (!StringUtils.hasText(inputHash)) {
            return;
        }
        aiCacheService.saveValidatedGenerated(context, inputHash, properties.effectiveTtlDays());
    }

    private boolean cacheReadable(AiFallbackCacheContext context) {
        return properties.isEnabled()
                && properties.isFallbackOnly()
                && context != null
                && StringUtils.hasText(context.getAssessmentType())
                && StringUtils.hasText(context.getBusinessType())
                && StringUtils.hasText(context.getBusinessId())
                && StringUtils.hasText(context.getPromptVersion());
    }

    private boolean cacheWritable(AiFallbackCacheContext context) {
        return cacheReadable(context)
                && properties.isWriteOnValidatedGenerated()
                && StringUtils.hasText(context.getOutputText());
    }
}
