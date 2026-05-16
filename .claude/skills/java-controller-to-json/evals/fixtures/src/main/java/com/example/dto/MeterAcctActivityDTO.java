package com.example.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
public class MeterAcctActivityDTO {

    @ApiModelProperty(value = "主键ID（更新时必填）")
    private Long id;

    @ApiModelProperty(value = "活动名称")
    private String activityName;

    @ApiModelProperty(value = "活动编码")
    private String activityCode;

    @ApiModelProperty(value = "开始日期")
    private Date startDate;

    @ApiModelProperty(value = "结束日期")
    private Date endDate;

    @ApiModelProperty(value = "活动状态 0-禁用 1-启用")
    private Integer status;

    @ApiModelProperty(value = "备注")
    private String remark;
}
