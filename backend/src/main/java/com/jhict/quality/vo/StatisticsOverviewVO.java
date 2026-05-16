package com.jhict.quality.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "StatisticsOverviewVO", description = "质量统计概览视图对象")
public class StatisticsOverviewVO {

    @ApiModelProperty(value = "检验总数")
    private long totalInspection;

    @ApiModelProperty(value = "合格数量")
    private long qualifiedCount;

    @ApiModelProperty(value = "不合格数量")
    private long unqualifiedCount;

    @ApiModelProperty(value = "需复检数量")
    private long needReinspectionCount;

    @ApiModelProperty(value = "可让步数量")
    private long canConcessionCount;

    @ApiModelProperty(value = "不合格率（百分比字符串，如\"12.34%\"）")
    private String unqualifiedRate;

    @ApiModelProperty(value = "复检率（百分比字符串）")
    private String reinspectionRate;

    @ApiModelProperty(value = "让步率（百分比字符串）")
    private String concessionRate;
}
