package com.jhict.quality.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AiAssessmentCreateCmd {

    private String assessmentType;
    private String businessType;
    private String businessId;
    private String relatedJudgmentId;
    private String inputSnapshot;
    private String referencesJson;
    private String modelProvider;
    private String modelName;
    private String promptVersion;
    private String rawOutput;
    private String structuredOutput;
    private String riskLevel;
    private BigDecimal confidenceScore;
    private String confidenceLabel;
    private String confidenceFactors;
    private String degradationSource;
    private Integer cacheHit;
    private String cacheKey;
}
