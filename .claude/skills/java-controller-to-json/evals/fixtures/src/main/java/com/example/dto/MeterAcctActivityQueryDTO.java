package com.example.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class MeterAcctActivityQueryDTO {

    @ApiModelProperty(value = "活动名称（模糊查询）")
    private String activityName;

    @ApiModelProperty(value = "活动状态 0-禁用 1-启用")
    private Integer status;

    @ApiModelProperty(value = "当前页码")
    private Integer pageNum = 1;

    @ApiModelProperty(value = "每页大小")
    private Integer pageSize = 10;
}
