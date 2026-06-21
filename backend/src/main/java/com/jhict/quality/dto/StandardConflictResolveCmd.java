package com.jhict.quality.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel(value = "StandardConflictResolveCmd", description = "标准冲突裁定")
public class StandardConflictResolveCmd {

    @NotBlank(message = "冲突状态不能为空")
    @ApiModelProperty(value = "裁定状态", required = true)
    private String conflictStatus;

    @ApiModelProperty(value = "裁定说明")
    private String resolutionNote;
}
