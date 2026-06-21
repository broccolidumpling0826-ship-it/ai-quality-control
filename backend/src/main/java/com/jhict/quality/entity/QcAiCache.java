package com.jhict.quality.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jhict.quality.common.entity.CoreEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("qc_ai_cache")
public class QcAiCache extends CoreEntity {

    private String cacheKey;
    private String assessmentType;
    private String businessType;
    private String businessId;
    private String promptVersion;
    private String inputHash;
    private String cachedOutput;
    private String referencesJson;
    private String confidenceLabel;
    private BigDecimal confidenceScore;
    private String degradationSource;
    private Integer enabled;
    private LocalDateTime expiryTime;
}
