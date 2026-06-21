package com.jhict.quality.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jhict.quality.common.entity.CoreEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("qc_ai_assessment")
public class QcAiAssessment extends CoreEntity {

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
    private String adoptionStatus;
    private String humanOpinion;
    private String handledBy;
    private LocalDateTime handledTime;
}
