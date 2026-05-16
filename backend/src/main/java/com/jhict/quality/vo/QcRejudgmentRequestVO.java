package com.jhict.quality.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@ApiModel(value = "QcRejudgmentRequestVO", description = "改判申请视图对象")
public class QcRejudgmentRequestVO {

    @ApiModelProperty(value = "改判申请ID")
    private String id;

    @ApiModelProperty(value = "原判定结论ID")
    private String originalJudgmentId;

    @ApiModelProperty(value = "原判定结论类型")
    private String originalJudgmentType;

    @ApiModelProperty(value = "目标判定结论类型")
    private String targetJudgmentType;

    @ApiModelProperty(value = "改判原因")
    private String rejudgmentReason;

    @ApiModelProperty(value = "影响范围")
    private String affectScope;

    @ApiModelProperty(value = "是否逆向改判：1是 0否")
    private Integer isReverse;

    @ApiModelProperty(value = "审批层级")
    private String approvalLevel;

    @ApiModelProperty(value = "审批状态")
    private String approvalStatus;

    @ApiModelProperty(value = "逆向改判标签（逆向改判时为\"逆向改判\"，否则为空串）")
    private String isReverseLabel;

    @ApiModelProperty(value = "审批记录列表")
    private List<ApprovalRecordVO> approvalRecords;

    @Data
    @ApiModel(value = "ApprovalRecordVO", description = "审批记录")
    public static class ApprovalRecordVO {

        @ApiModelProperty(value = "审批人工号")
        private String approverNo;

        @ApiModelProperty(value = "审批动作")
        private String approvalAction;

        @ApiModelProperty(value = "审批意见")
        private String approvalComment;

        @ApiModelProperty(value = "审批时间")
        private LocalDateTime approvalTime;
    }
}
