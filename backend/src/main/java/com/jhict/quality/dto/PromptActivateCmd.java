package com.jhict.quality.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel(value = "PromptActivateCmd", description = "Prompt 版本激活")
public class PromptActivateCmd {

    @NotBlank(message = "版本号不能为空")
    @ApiModelProperty(value = "版本号", required = true)
    private String versionNo;
}
