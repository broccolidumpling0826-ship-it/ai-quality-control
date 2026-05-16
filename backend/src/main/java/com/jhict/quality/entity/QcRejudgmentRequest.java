package com.jhict.quality.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jhict.quality.common.entity.CoreEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("qc_rejudgment_request")
@ApiModel(value = "QcRejudgmentRequest", description = "改判申请")
public class QcRejudgmentRequest extends CoreEntity {

    @ApiModelProperty(value = "原判定结论ID")
    private String originalJudgmentId;

    @ApiModelProperty(value = "原判定结论类型")
    private String originalJudgmentType;

    @ApiModelProperty(value = "目标判定结论类型")
    private String targetJudgmentType;

    @ApiModelProperty(value = "改判原因（最多1000字）")
    private String rejudgmentReason;

    @ApiModelProperty(value = "影响范围（最多1000字）")
    private String affectScope;

    @ApiModelProperty(value = "是否逆向改判：1是 0否")
    private Integer isReverse;

    @ApiModelProperty(value = "新证据来源")
    private String newEvidenceSource;

    @ApiModelProperty(value = "证据附件URL")
    private String evidenceAttachmentUrl;

    @ApiModelProperty(value = "审批层级（NORMAL/ENHANCED）")
    private String approvalLevel;

    @ApiModelProperty(value = "审批状态（PENDING/APPROVED/REJECTED）")
    private String approvalStatus;
}
