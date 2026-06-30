package com.jhict.quality.service.api;

import com.jhict.quality.entity.QcAiCache;
import com.jhict.quality.service.support.ai.AiFallbackCacheContext;

public interface AiCacheService {

    QcAiCache findActive(String assessmentType, String businessType, String businessId);

    /**
     * Temporary Task 2 bridge for fallback cache lookup; full versioned cache behavior is implemented in Task 3.
     */
    QcAiCache findActive(String assessmentType, String businessType, String businessId,
                         String promptVersion, String inputHash);

    /**
     * Temporary Task 2 bridge for validated generated writes; full persistence is implemented in Task 3.
     */
    void saveValidatedGenerated(AiFallbackCacheContext context, String inputHash, int ttlDays);
}
