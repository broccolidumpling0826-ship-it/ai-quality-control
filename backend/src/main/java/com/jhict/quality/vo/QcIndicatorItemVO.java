package com.jhict.quality.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "QcIndicatorItemVO", description = "指标项目视图对象")
public class QcIndicatorItemVO {

    @ApiModelProperty(value = "指标ID")
    private String id;

    @ApiModelProperty(value = "指标名称")
    private String indicatorName;

    @ApiModelProperty(value = "指标代码")
    private String indicatorCode;

    @ApiModelProperty(value = "指标类别")
    private String indicatorCategory;

    @ApiModelProperty(value = "指标类别（前端别名）")
    private String category;

    @ApiModelProperty(value = "单位")
    private String unit;

    @ApiModelProperty(value = "检测方法")
    private String testMethod;

    @ApiModelProperty(value = "描述")
    private String description;

    @ApiModelProperty(value = "备注（前端别名）")
    private String remark;

    @ApiModelProperty(value = "状态 ACTIVE/INACTIVE")
    private String status;
}
