package com.jhict.performance.api.query;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * KPI目标设置 查询参数
 */
@Getter
@Setter
public class PrfKpiTargetQuery extends PageQuery {

    @ApiModelProperty(value = "KPI编码")
    private String kpiCode;

    @ApiModelProperty(value = "KPI名称")
    private String kpiName;

    @ApiModelProperty(value = "年份")
    private String yearNo;

    @ApiModelProperty(value = "月份")
    private String monthNo;

    // 按需添加其他查询字段
}
