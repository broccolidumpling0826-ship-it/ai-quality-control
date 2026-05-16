package com.jhict.quality.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "IndicatorDistributionVO", description = "指标不合格分布视图对象")
public class IndicatorDistributionVO {

    @ApiModelProperty(value = "指标名称")
    private String indicatorName;

    @ApiModelProperty(value = "不合格次数")
    private long failCount;

    @ApiModelProperty(value = "不合格率（百分比字符串，如\"8.50%\"）")
    private String failRate;
}
