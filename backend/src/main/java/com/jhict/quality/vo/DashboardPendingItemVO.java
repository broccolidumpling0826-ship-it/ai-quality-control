package com.jhict.quality.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "DashboardPendingItemVO", description = "工作台待处理事项")
public class DashboardPendingItemVO {

    @ApiModelProperty(value = "业务ID")
    private String id;

    @ApiModelProperty(value = "类型 judgment/reinspection/concession")
    private String type;

    @ApiModelProperty(value = "批次号")
    private String batchNo;

    @ApiModelProperty(value = "描述")
    private String description;

    @ApiModelProperty(value = "优先级 high/medium/low")
    private String priority;

    @ApiModelProperty(value = "创建时间")
    private String createTime;

    @ApiModelProperty(value = "指派给（工号，可为空）")
    private String assignTo;
}
