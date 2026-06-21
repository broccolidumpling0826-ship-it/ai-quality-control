package com.jhict.quality.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel(value = "ConcessionRiskAssessCmd", description = "让步风险评估命令")
public class ConcessionRiskAssessCmd {

    @NotBlank(message = "判定ID不能为空")
    @ApiModelProperty(value = "判定ID", required = true)
    private String judgmentId;

    @ApiModelProperty(value = "让步申请ID，可为空")
    private String concessionId;

    @ApiModelProperty(value = "人工补充客户用途")
    private String customerUsage;

    @ApiModelProperty(value = "人工补充用途风险 NORMAL/HIGH_FORMING/SAFETY_CRITICAL")
    private String usageRiskCategory;

    @ApiModelProperty(value = "替代资源交付窗口天数，默认7")
    private Integer deliveryWindowDays;
}
