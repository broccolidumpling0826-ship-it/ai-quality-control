package com.jhict.quality.service.api;

import com.jhict.quality.dto.AiDegradationRequest;
import com.jhict.quality.vo.AiDegradationResultVO;

/**
 * Resolves AI output using the configured degradation order.
 */
public interface AiDegradationService {

    /**
     * Resolve one AI output in the order cache, generated text, rule template,
     * raw retrieval, then unavailable state.
     *
     * @param request degradation input assembled by the calling business service
     * @return resolved output and degradation marker
     */
    AiDegradationResultVO resolveAiOutput(AiDegradationRequest request);
}
