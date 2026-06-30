package com.jhict.quality.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jhict.quality.entity.QcAiCache;
import com.jhict.quality.mapper.QcAiCacheMapper;
import com.jhict.quality.service.api.AiCacheService;
import com.jhict.quality.service.support.ai.AiFallbackCacheContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AiCacheServiceImpl implements AiCacheService {

    @Resource
    private QcAiCacheMapper aiCacheMapper;

    @Override
    public QcAiCache findActive(String assessmentType, String businessType, String businessId) {
        return aiCacheMapper.selectList(new LambdaQueryWrapper<QcAiCache>()
                        .eq(QcAiCache::getAssessmentType, assessmentType)
                        .eq(QcAiCache::getBusinessType, businessType)
                        .eq(QcAiCache::getBusinessId, businessId)
                        .eq(QcAiCache::getEnabled, 1)
                        .and(w -> w.isNull(QcAiCache::getExpiryTime)
                                .or()
                                .ge(QcAiCache::getExpiryTime, LocalDateTime.now()))
                        .orderByDesc(QcAiCache::getCreateDateTime))
                .stream()
                .findFirst()
                .orElse(null);
    }

    @Override
    public QcAiCache findActive(String assessmentType, String businessType, String businessId,
                                String promptVersion, String inputHash) {
        return aiCacheMapper.selectList(new LambdaQueryWrapper<QcAiCache>()
                        .eq(QcAiCache::getAssessmentType, assessmentType)
                        .eq(QcAiCache::getBusinessType, businessType)
                        .eq(QcAiCache::getBusinessId, businessId)
                        .eq(QcAiCache::getPromptVersion, promptVersion)
                        .eq(QcAiCache::getInputHash, inputHash)
                        .eq(QcAiCache::getEnabled, 1)
                        .and(w -> w.isNull(QcAiCache::getExpiryTime)
                                .or()
                                .ge(QcAiCache::getExpiryTime, LocalDateTime.now()))
                        .orderByDesc(QcAiCache::getCreateDateTime))
                .stream()
                .findFirst()
                .orElse(null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveValidatedGenerated(AiFallbackCacheContext context, String inputHash, int ttlDays) {
        List<QcAiCache> activeCaches = aiCacheMapper.selectList(new LambdaQueryWrapper<QcAiCache>()
                .eq(QcAiCache::getAssessmentType, context.getAssessmentType())
                .eq(QcAiCache::getBusinessType, context.getBusinessType())
                .eq(QcAiCache::getBusinessId, context.getBusinessId())
                .eq(QcAiCache::getPromptVersion, context.getPromptVersion())
                .eq(QcAiCache::getInputHash, inputHash)
                .eq(QcAiCache::getEnabled, 1));
        for (QcAiCache cache : activeCaches) {
            cache.setEnabled(0);
            aiCacheMapper.updateById(cache);
        }

        QcAiCache cache = new QcAiCache();
        cache.setCacheKey(buildCacheKey(context, inputHash));
        cache.setAssessmentType(context.getAssessmentType());
        cache.setBusinessType(context.getBusinessType());
        cache.setBusinessId(context.getBusinessId());
        cache.setPromptVersion(context.getPromptVersion());
        cache.setInputHash(inputHash);
        cache.setCachedOutput(context.getOutputText());
        cache.setReferencesJson(context.getStructuredOutput());
        cache.setConfidenceLabel(context.getConfidenceLabel());
        cache.setConfidenceScore(context.getConfidenceScore());
        cache.setDegradationSource("CACHE");
        cache.setEnabled(1);
        cache.setExpiryTime(LocalDateTime.now().plusDays(ttlDays));
        aiCacheMapper.insert(cache);
    }

    private String buildCacheKey(AiFallbackCacheContext context, String inputHash) {
        return context.getAssessmentType() + ":" + context.getBusinessType() + ":"
                + context.getBusinessId() + ":" + context.getPromptVersion() + ":" + inputHash;
    }
}
