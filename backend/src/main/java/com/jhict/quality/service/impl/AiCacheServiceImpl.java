package com.jhict.quality.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jhict.quality.entity.QcAiCache;
import com.jhict.quality.mapper.QcAiCacheMapper;
import com.jhict.quality.service.api.AiCacheService;
import com.jhict.quality.service.support.ai.AiFallbackCacheContext;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;

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
    public void saveValidatedGenerated(AiFallbackCacheContext context, String inputHash, int ttlDays) {
        // Full versioned cache persistence is intentionally deferred to Task 3.
    }
}
