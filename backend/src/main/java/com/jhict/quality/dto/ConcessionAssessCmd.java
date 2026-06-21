package com.jhict.quality.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel(value = "ConcessionAssessCmd", description = "AI 让步评估请求")
public class ConcessionAssessCmd {

    @NotBlank(message = "判定ID不能为空")
    @ApiModelProperty(value = "判定结论ID", required = true)
    private String judgmentId;

    @ApiModelProperty(value = "让步申请ID")
    private String concessionId;
}
