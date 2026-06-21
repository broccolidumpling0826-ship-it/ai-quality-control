package com.jhict.quality.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jhict.quality.common.entity.CoreEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("qc_ai_confidence_config")
public class QcAiConfidenceConfig extends CoreEntity {

    private String configName;
    private BigDecimal ruleWeight;
    private BigDecimal ragWeight;
    private BigDecimal llmWeight;
    private BigDecimal highThreshold;
    private BigDecimal mediumThreshold;
    private BigDecimal lowThreshold;
    private Integer enabled;
    private Integer activeFlag;
    private String updatedBy;
    private LocalDateTime updatedAt;
    private String remark;
}
