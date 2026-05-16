package com.jhict.quality.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.math.BigDecimal;
import java.util.List;

@Data
@ApiModel(value = "QcInspectionRecordAddCmd", description = "新增检验记录命令")
public class QcInspectionRecordAddCmd {

    @ApiModelProperty(value = "炉号", required = true)
    @NotBlank(message = "炉号不能为空")
    private String heatNo;

    @ApiModelProperty(value = "卷号", required = true)
    @NotBlank(message = "卷号不能为空")
    private String coilNo;

    @ApiModelProperty(value = "客户ID")
    private String customerId;

    @ApiModelProperty(value = "品种", required = true)
    @NotBlank(message = "品种不能为空")
    private String productVariety;

    @ApiModelProperty(value = "牌号", required = true)
    @NotBlank(message = "牌号不能为空")
    private String productGrade;

    @ApiModelProperty(value = "规格", required = true)
    @NotBlank(message = "规格不能为空")
    private String productSpec;

    @ApiModelProperty(value = "样品类型", required = true)
    @NotBlank(message = "样品类型不能为空")
    private String sampleType;

    @ApiModelProperty(value = "检验时间（yyyy-MM-dd HH:mm:ss）", required = true)
    @NotBlank(message = "检验时间不能为空")
    private String testTime;

    @ApiModelProperty(value = "检验人工号", required = true)
    @NotBlank(message = "检验人工号不能为空")
    private String testerNo;

    @ApiModelProperty(value = "检验值列表", required = true)
    @NotEmpty(message = "检验值不能为空")
    private List<InspectionValueItem> values;

    @Data
    @ApiModel(value = "InspectionValueItem", description = "单项检验值")
    public static class InspectionValueItem {

        @ApiModelProperty(value = "指标ID", required = true)
        @NotBlank(message = "指标ID不能为空")
        private String indicatorId;

        @ApiModelProperty(value = "实测值（数值型）")
        private BigDecimal testValue;

        @ApiModelProperty(value = "文本值（文本型）")
        private String valueText;
    }
}
