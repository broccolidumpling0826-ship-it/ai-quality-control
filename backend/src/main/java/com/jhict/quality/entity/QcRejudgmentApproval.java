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
@TableName("qc_rejudgment_approval")
@ApiModel(value = "QcRejudgmentApproval", description = "改判审批记录")
public class QcRejudgmentApproval extends CoreEntity {

    @ApiModelProperty(value = "改判申请ID")
    private String requestId;

    @ApiModelProperty(value = "审批人工号")
    private String approverNo;

    @ApiModelProperty(value = "审批动作（APPROVED/REJECTED）")
    private String approvalAction;

    @ApiModelProperty(value = "审批意见（最多1000字）")
    private String approvalComment;

    @ApiModelProperty(value = "审批时间")
    private LocalDateTime approvalTime;
}
