package com.jhict.quality.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jhict.quality.common.entity.CoreEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("qc_concession_assessment")
@ApiModel(value = "QcConcessionAssessment", description = "AI 让步评估")
public class QcConcessionAssessment extends CoreEntity {

    @ApiModelProperty(value = "判定ID")
    private String judgmentId;

    @ApiModelProperty(value = "让步申请ID")
    private String concessionId;

    @ApiModelProperty(value = "风险等级")
    private String riskLevel;

    @ApiModelProperty(value = "客户影响")
    private String customerImpact;

    @ApiModelProperty(value = "建议接收条件")
    private String suggestedConditions;

    @ApiModelProperty(value = "历史案例 JSON")
    private String historicalCases;

    @ApiModelProperty(value = "置信度")
    private String confidenceLevel;

    @ApiModelProperty(value = "是否降级")
    private Integer degraded;

    @ApiModelProperty(value = "审计日志ID")
    private String auditLogId;
}
