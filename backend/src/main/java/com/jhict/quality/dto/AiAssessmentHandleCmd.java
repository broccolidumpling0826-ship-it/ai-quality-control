package com.jhict.quality.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel(value = "AiAssessmentHandleCmd", description = "AI评估处理命令")
public class AiAssessmentHandleCmd {

    @NotBlank(message = "处理状态不能为空")
    @ApiModelProperty(value = "处理状态 ADOPTED/IGNORED", required = true)
    private String adoptionStatus;

    @ApiModelProperty(value = "人工意见")
    private String humanOpinion;
}
