package com.jhict.quality.dto;

import com.jhict.quality.vo.AiSourceReferenceVO;
import lombok.Data;

import java.util.List;

/**
 * Request for resolving a layered AI output fallback.
 */
@Data
public class AiDegradationRequest {

    private String assessmentType;

    private String businessType;

    private String businessId;

    private String cacheKey;

    private String cachedOutput;

    private String generatedOutput;

    private String ruleTemplateOutput;

    private List<AiSourceReferenceVO> rawReferences;

    private String confidenceLabel;

    private Double confidenceScore;

    private String unavailableReason;
}
