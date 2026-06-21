package com.jhict.quality.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel(value = "QcRejudgmentRequestAddCmd", description = "发起改判申请命令")
public class QcRejudgmentRequestAddCmd {

    @NotBlank(message = "原判定结论ID不能为空")
    @ApiModelProperty(value = "原判定结论ID", required = true)
    private String originalJudgmentId;

    @NotBlank(message = "目标判定结论类型不能为空")
    @ApiModelProperty(value = "目标判定结论类型（不可选择 STANDARD_CONFLICT）", required = true)
    private String targetJudgmentType;

    @NotBlank(message = "改判原因不能为空")
    @ApiModelProperty(value = "改判原因", required = true)
    private String rejudgmentReason;

    @NotBlank(message = "影响范围不能为空")
    @ApiModelProperty(value = "影响范围", required = true)
    private String affectScope;

    @ApiModelProperty(value = "新证据来源（逆向改判时必填）")
    private String newEvidenceSource;

    @ApiModelProperty(value = "证据附件URL（逆向改判时必填）")
    private String evidenceAttachmentUrl;
}
