package com.jhict.quality.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel(value = "CertQaQueryCmd", description = "质保书问答请求")
public class CertQaQueryCmd {

    @ApiModelProperty(value = "卷号")
    private String coilNo;

    @ApiModelProperty(value = "批次号")
    private String batchNo;

    @NotBlank(message = "问题不能为空")
    @ApiModelProperty(value = "问题", required = true)
    private String question;
}
