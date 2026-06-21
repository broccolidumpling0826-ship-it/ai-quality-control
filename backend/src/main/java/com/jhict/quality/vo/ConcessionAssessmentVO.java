package com.jhict.quality.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@ApiModel(value = "ConcessionAssessmentVO", description = "AI 让步评估结果")
public class ConcessionAssessmentVO {

    @ApiModelProperty(value = "评估ID")
    private String id;

    @ApiModelProperty(value = "判定ID")
    private String judgmentId;

    @ApiModelProperty(value = "风险等级 LOW/MEDIUM/HIGH")
    private String riskLevel;

    @ApiModelProperty(value = "客户影响")
    private String customerImpact;

    @ApiModelProperty(value = "建议接收条件")
    private String suggestedConditions;

    @ApiModelProperty(value = "历史案例")
    private String historicalCases;

    @ApiModelProperty(value = "置信度")
    private String confidenceLevel;

    @ApiModelProperty(value = "是否降级")
    private Boolean degraded;

    @ApiModelProperty(value = "审计日志ID")
    private String auditLogId;
}
