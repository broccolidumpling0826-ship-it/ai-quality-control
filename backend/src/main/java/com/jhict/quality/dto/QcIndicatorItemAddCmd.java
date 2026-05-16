package com.jhict.quality.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel(value = "QcIndicatorItemAddCmd", description = "指标项目新增/更新命令")
public class QcIndicatorItemAddCmd {

    @ApiModelProperty(value = "指标ID（更新时由路径传入，可不填）")
    private String id;

    @NotBlank(message = "指标名称不能为空")
    @ApiModelProperty(value = "指标名称", required = true)
    private String indicatorName;

    @NotBlank(message = "指标代码不能为空")
    @ApiModelProperty(value = "指标代码", required = true)
    private String indicatorCode;

    @NotBlank(message = "指标类别不能为空")
    @ApiModelProperty(value = "指标类别", required = true)
    private String category;

    @ApiModelProperty(value = "单位")
    private String unit;

    @ApiModelProperty(value = "检测方法")
    private String testMethod;

    @ApiModelProperty(value = "备注")
    private String remark;
}
