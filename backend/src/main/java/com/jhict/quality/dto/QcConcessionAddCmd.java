package com.jhict.quality.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotBlank;
import java.time.LocalDate;

@Data
@ApiModel(value = "QcConcessionAddCmd", description = "发起让步接收申请命令")
public class QcConcessionAddCmd {

    @NotBlank(message = "判定结论ID不能为空")
    @ApiModelProperty(value = "判定结论ID", required = true)
    private String judgmentId;

    @NotBlank(message = "让步范围不能为空")
    @ApiModelProperty(value = "让步范围", required = true)
    private String concessionScope;

    @NotBlank(message = "风险描述不能为空")
    @ApiModelProperty(value = "风险描述", required = true)
    private String riskDescription;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @ApiModelProperty(value = "生效日期")
    private LocalDate effectiveDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @ApiModelProperty(value = "到期日期")
    private LocalDate expiryDate;
}
