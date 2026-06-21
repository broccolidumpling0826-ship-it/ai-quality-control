package com.jhict.quality.service.api;

import com.jhict.quality.entity.QcAiCache;

public interface AiCacheService {

    QcAiCache findActive(String assessmentType, String businessType, String businessId);
}
