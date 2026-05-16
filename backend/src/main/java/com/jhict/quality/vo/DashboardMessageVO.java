package com.jhict.quality.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "DashboardMessageVO", description = "工作台站内消息")
public class DashboardMessageVO {

    @ApiModelProperty(value = "消息ID")
    private String id;

    @ApiModelProperty(value = "标题")
    private String title;

    @ApiModelProperty(value = "内容")
    private String content;

    @ApiModelProperty(value = "级别 info/warning/success/error")
    private String level;

    @ApiModelProperty(value = "创建时间")
    private String createTime;

    @ApiModelProperty(value = "是否已读")
    private Boolean isRead;
}
