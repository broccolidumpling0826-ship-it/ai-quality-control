package com.jhict.quality.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel(value = "StandardConflictResolveCmd", description = "标准冲突裁决命令")
public class StandardConflictResolveCmd {

    @NotBlank(message = "裁决控制标准不能为空")
    @ApiModelProperty(value = "裁决控制标准ID", required = true)
    private String decisionStandardId;

    @NotBlank(message = "裁决理由不能为空")
    @ApiModelProperty(value = "裁决理由", required = true)
    private String decisionReason;
}
