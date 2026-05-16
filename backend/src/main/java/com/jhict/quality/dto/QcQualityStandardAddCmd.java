package com.jhict.quality.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@ApiModel(value = "QcQualityStandardAddCmd", description = "新增/更新质量标准命令")
public class QcQualityStandardAddCmd {

    @ApiModelProperty(value = "标准ID（更新时必填）")
    private String id;

    @ApiModelProperty(value = "标准类型（NATIONAL/ENTERPRISE/CUSTOMER）", required = true)
    @NotBlank(message = "标准类型不能为空")
    private String standardType;

    @ApiModelProperty(value = "品种", required = true)
    @NotBlank(message = "品种不能为空")
    private String variety;

    @ApiModelProperty(value = "牌号", required = true)
    @NotBlank(message = "牌号不能为空")
    private String grade;

    @ApiModelProperty(value = "规格范围", required = true)
    @NotBlank(message = "规格范围不能为空")
    private String specRange;

    @ApiModelProperty(value = "版本号", required = true)
    @NotBlank(message = "版本号不能为空")
    private String versionNo;

    @ApiModelProperty(value = "生效日期", required = true)
    @NotNull(message = "生效日期不能为空")
    private LocalDate effectiveDate;

    @ApiModelProperty(value = "失效日期", required = true)
    @NotNull(message = "失效日期不能为空")
    private LocalDate expiryDate;

    @ApiModelProperty(value = "客户ID（客户协议标准必填）")
    private String customerId;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "标准指标列表", required = true)
    @NotEmpty(message = "标准指标不能为空")
    private List<StandardIndicatorItem> indicators;

    @Data
    @ApiModel(value = "StandardIndicatorItem", description = "标准指标配置项")
    public static class StandardIndicatorItem {

        @ApiModelProperty(value = "指标ID", required = true)
        @NotBlank(message = "指标ID不能为空")
        private String indicatorId;

        @ApiModelProperty(value = "上限")
        private BigDecimal upperLimit;

        @ApiModelProperty(value = "下限")
        private BigDecimal lowerLimit;

        @ApiModelProperty(value = "是否必检：1必检 0非必检")
        private Integer isRequired;

        @ApiModelProperty(value = "让步上限")
        private BigDecimal concessionUpper;

        @ApiModelProperty(value = "让步下限")
        private BigDecimal concessionLower;
    }
}
