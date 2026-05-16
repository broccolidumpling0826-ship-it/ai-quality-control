package com.jhict.quality.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel(value = "QcReinspectionAddCmd", description = "发起复检命令")
public class QcReinspectionAddCmd {

    @NotBlank(message = "原判定结论ID不能为空")
    @ApiModelProperty(value = "原判定结论ID", required = true)
    private String originalJudgmentId;

    @NotBlank(message = "复检原因不能为空")
    @ApiModelProperty(value = "复检原因", required = true)
    private String reinspectionReason;

    @ApiModelProperty(value = "责任人工号")
    private String responsibleNo;
}
