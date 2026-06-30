package com.jhict.quality.service.support.ai;

import com.jhict.quality.entity.QcAiCache;

public interface AiFallbackCacheService {

    String buildInputHash(AiFallbackCacheContext context);

    QcAiCache findFallbackCache(AiFallbackCacheContext context);

    void saveValidatedGenerated(AiFallbackCacheContext context);
}
