package com.jhict.quality.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "QcRejudgmentListVO", description = "改判申请列表视图")
public class QcRejudgmentListVO {

    @ApiModelProperty(value = "改判申请ID")
    private String id;

    @ApiModelProperty(value = "原判定结论类型")
    private String originalJudgmentType;

    @ApiModelProperty(value = "目标判定结论类型")
    private String targetJudgmentType;

    @ApiModelProperty(value = "改判原因")
    private String rejudgmentReason;

    @ApiModelProperty(value = "是否逆向改判：1是 0否")
    private Integer isReverse;

    @ApiModelProperty(value = "审批层级")
    private String approvalLevel;

    @ApiModelProperty(value = "审批状态")
    private String approvalStatus;

    @ApiModelProperty(value = "卷号")
    private String coilNo;

    @ApiModelProperty(value = "批次号")
    private String batchNo;

    @ApiModelProperty(value = "申请时间")
    private String createDateTime;

    @ApiModelProperty(value = "申请人工号")
    private String createUserNo;
}
