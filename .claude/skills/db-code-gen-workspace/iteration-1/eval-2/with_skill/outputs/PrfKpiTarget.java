package com.jhict.performance.api.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jhict.common.core.entity.CoreEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * KPI目标设置
 */
@Getter
@Setter
@TableName("PRF_KPI_TARGET")
public class PrfKpiTarget extends CoreEntity {

    @ApiModelProperty(value = "KPI编码")
    private String kpiCode;

    @ApiModelProperty(value = "KPI名称")
    private String kpiName;

    @ApiModelProperty(value = "目标值")
    private BigDecimal targetValue;

    @ApiModelProperty(value = "单位")
    private String unit;

    @ApiModelProperty(value = "年份")
    private String yearNo;

    @ApiModelProperty(value = "月份")
    private String monthNo;

    @ApiModelProperty(value = "是否启用")
    private Boolean enable;
}
