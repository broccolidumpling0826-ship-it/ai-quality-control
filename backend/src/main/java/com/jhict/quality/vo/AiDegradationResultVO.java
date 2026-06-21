package com.jhict.quality.vo;

import lombok.Data;

import java.util.List;

/**
 * Resolved AI output with degradation marker and references.
 */
@Data
public class AiDegradationResultVO {

    private String assessmentType;

    private String businessType;

    private String businessId;

    private String cacheKey;

    private String outputText;

    private String degradationSource;

    private String degradationReason;

    private String confidenceLabel;

    private Double confidenceScore;

    private Boolean authoritative;

    private List<AiSourceReferenceVO> references;
}
