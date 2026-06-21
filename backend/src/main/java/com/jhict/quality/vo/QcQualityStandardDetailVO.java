package com.jhict.quality.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "QcQualityStandardDetailVO", description = "质量标准详情视图对象")
public class QcQualityStandardDetailVO extends QcQualityStandardVO {

    @ApiModelProperty(value = "标准指标列表")
    private List<StandardIndicatorDetail> indicators;

    @ApiModelProperty(value = "关联源 PDF 摘要")
    private StandardSourceDocumentVO sourceDocument;

    @Data
    @ApiModel(value = "StandardIndicatorDetail", description = "标准指标详情")
    public static class StandardIndicatorDetail {

        @ApiModelProperty(value = "指标ID")
        private String indicatorId;

        @ApiModelProperty(value = "指标名称")
        private String indicatorName;

        @ApiModelProperty(value = "指标代码")
        private String indicatorCode;

        @ApiModelProperty(value = "指标类别")
        private String category;

        @ApiModelProperty(value = "单位")
        private String unit;

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
