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
@TableName("qc_inspection_value")
@ApiModel(value = "QcInspectionValue", description = "检验值")
public class QcInspectionValue extends CoreEntity {

    @ApiModelProperty(value = "关联检验记录ID")
    private String recordId;

    @ApiModelProperty(value = "关联指标项目ID")
    private String indicatorId;

    @ApiModelProperty(value = "实测值（数值型指标）")
    private BigDecimal testValue;

    @ApiModelProperty(value = "文本值（文本型指标）")
    private String valueText;
}
