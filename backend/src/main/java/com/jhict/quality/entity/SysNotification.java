package com.jhict.quality.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jhict.quality.common.entity.CoreEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_notification")
@ApiModel(value = "SysNotification", description = "站内通知")
public class SysNotification extends CoreEntity {

    @ApiModelProperty(value = "接收人工号")
    private String receiverNo;

    @ApiModelProperty(value = "标题（最多200字）")
    private String title;

    @ApiModelProperty(value = "内容（最多1000字）")
    private String content;

    @ApiModelProperty(value = "关联业务类型")
    private String relatedType;

    @ApiModelProperty(value = "关联业务ID")
    private String relatedId;

    @ApiModelProperty(value = "是否已读：0未读 1已读")
    private Integer isRead;

    @ApiModelProperty(value = "阅读时间")
    private LocalDateTime readTime;
}
