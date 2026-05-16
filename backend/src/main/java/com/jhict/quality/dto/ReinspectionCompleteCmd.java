package com.jhict.quality.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel(value = "ReinspectionCompleteCmd", description = "完成复检命令")
public class ReinspectionCompleteCmd {

    @NotBlank(message = "新检验记录ID不能为空")
    @ApiModelProperty(value = "新检验记录ID", required = true)
    private String newRecordId;
}
