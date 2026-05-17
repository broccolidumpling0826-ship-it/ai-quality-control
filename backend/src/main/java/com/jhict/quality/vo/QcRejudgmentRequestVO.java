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

    @ApiModelProperty(value = "卷号")
    private String coilNo;

    @ApiModelProperty(value = "批次号")
    private String batchNo;

    @ApiModelProperty(value = "炉号")
    private String heatNo;

    @ApiModelProperty(value = "申请人工号")
    private String applyBy;

    @ApiModelProperty(value = "申请人姓名")
    private String applyByName;

    @ApiModelProperty(value = "申请时间")
    private String applyTime;

    @ApiModelProperty(value = "原判定结论类型")
    private String originalJudgmentType;

    @ApiModelProperty(value = "目标判定结论类型")
    private String targetJudgmentType;

    @ApiModelProperty(value = "改判原因")
    private String rejudgmentReason;

    @ApiModelProperty(value = "改判原因（前端别名 reason）")
    private String reason;

    @ApiModelProperty(value = "影响范围")
    private String affectScope;

    @ApiModelProperty(value = "影响范围（前端别名 impactScope）")
    private String impactScope;

    @ApiModelProperty(value = "新证据来源（前端别名 evidenceSource）")
    private String evidenceSource;

    @ApiModelProperty(value = "证据附件（前端别名 evidenceFileUrl）")
    private String evidenceFileUrl;

    @ApiModelProperty(value = "审批记录（前端别名 approvalHistory）")
    private List<ApprovalRecordVO> approvalHistory;

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

        @ApiModelProperty(value = "审批动作（前端别名 decision）")
        private String decision;

        @ApiModelProperty(value = "审批意见")
        private String approvalComment;

        @ApiModelProperty(value = "审批意见（前端别名 comment）")
        private String comment;

        @ApiModelProperty(value = "审批时间")
        private LocalDateTime approvalTime;

        @ApiModelProperty(value = "审批时间（前端别名 approveTime）")
        private LocalDateTime approveTime;
    }
}
