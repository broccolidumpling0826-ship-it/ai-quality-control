package com.jhict.quality.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jhict.quality.common.entity.CoreEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("qc_judgment_evidence")
@ApiModel(value = "QcJudgmentEvidence", description = "判定依据（快照）")
public class QcJudgmentEvidence extends CoreEntity {

    @ApiModelProperty(value = "关联判定结论ID")
    private String judgmentId;

    @ApiModelProperty(value = "关联质量标准ID（快照）")
    private String standardId;

    @ApiModelProperty(value = "关联指标项目ID（快照）")
    private String indicatorId;

    @ApiModelProperty(value = "实测值（快照）")
    private BigDecimal testValue;

    @ApiModelProperty(value = "标准上限（快照）")
    private BigDecimal upperLimit;

    @ApiModelProperty(value = "标准下限（快照）")
    private BigDecimal lowerLimit;

    @ApiModelProperty(value = "偏差值（实测值 - 超出的限值）")
    private BigDecimal deviation;

    @ApiModelProperty(value = "触发规则描述")
    private String triggerRule;

    @ApiModelProperty(value = "该指标是否通过：1通过 0未通过")
    private Integer isPassed;
}
