package com.jhict.quality.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
@ApiModel(value = "RejudgmentApproveCmd", description = "改判审批命令")
public class RejudgmentApproveCmd {

    @NotBlank(message = "审批动作不能为空")
    @Pattern(regexp = "APPROVED|REJECTED", message = "审批动作必须为 APPROVED 或 REJECTED")
    @ApiModelProperty(value = "审批动作（APPROVED|REJECTED）", required = true)
    private String action;

    @ApiModelProperty(value = "审批意见")
    private String comment;
}
