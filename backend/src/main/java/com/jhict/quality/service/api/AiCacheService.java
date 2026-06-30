package com.jhict.quality.service.api;

import com.jhict.quality.entity.QcAiCache;
import com.jhict.quality.service.support.ai.AiFallbackCacheContext;

public interface AiCacheService {

    QcAiCache findActive(String assessmentType, String businessType, String businessId);

    QcAiCache findActive(String assessmentType, String businessType, String businessId,
                         String promptVersion, String inputHash);

    void saveValidatedGenerated(AiFallbackCacheContext context, String inputHash, int ttlDays);
}
