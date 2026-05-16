package com.jhict.quality.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
@ApiModel(value = "QcQualityCertGenerateCmd", description = "生成质保书数据命令")
public class QcQualityCertGenerateCmd {

    @NotBlank(message = "查询类型不能为空")
    @Pattern(regexp = "COIL|BATCH", message = "查询类型必须为 COIL 或 BATCH")
    @ApiModelProperty(value = "查询类型（COIL-按卷号，BATCH-按批次号）", required = true)
    private String queryType;

    @ApiModelProperty(value = "卷号（queryType=COIL时必填）")
    private String coilNo;

    @ApiModelProperty(value = "批次号（queryType=BATCH时必填）")
    private String batchNo;
}
