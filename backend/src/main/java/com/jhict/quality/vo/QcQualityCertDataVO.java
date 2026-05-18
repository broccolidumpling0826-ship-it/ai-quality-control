package com.jhict.quality.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@ApiModel(value = "QcQualityCertDataVO", description = "质保书数据视图对象")
public class QcQualityCertDataVO {

    @ApiModelProperty(value = "质保书数据ID")
    private String id;

    @ApiModelProperty(value = "卷号")
    private String coilNo;

    @ApiModelProperty(value = "批次号")
    private String batchNo;

    @ApiModelProperty(value = "生成时间")
    private LocalDateTime generateTime;

    @ApiModelProperty(value = "生成人工号")
    private String generatedBy;

    @ApiModelProperty(value = "炉号")
    private String heatNo;

    @ApiModelProperty(value = "品种")
    private String productVariety;

    @ApiModelProperty(value = "牌号")
    private String productGrade;

    @ApiModelProperty(value = "客户ID")
    private String customerId;

    @ApiModelProperty(value = "最终判定结论")
    private String finalJudgmentType;

    @ApiModelProperty(value = "生成状态：SUCCESS/FAILED")
    private String status;

    @ApiModelProperty(value = "指标快照列表")
    private List<IndicatorSnapshot> indicators;

    @Data
    @ApiModel(value = "IndicatorSnapshot", description = "指标快照")
    public static class IndicatorSnapshot {

        @ApiModelProperty(value = "指标名称")
        private String indicatorName;

        @ApiModelProperty(value = "指标代码")
        private String indicatorCode;

        @ApiModelProperty(value = "单位")
        private String unit;

        @ApiModelProperty(value = "实测值")
        private BigDecimal testValue;

        @ApiModelProperty(value = "标准上限")
        private BigDecimal upperLimit;

        @ApiModelProperty(value = "标准下限")
        private BigDecimal lowerLimit;

        @ApiModelProperty(value = "是否通过：1通过 0未通过")
        private Integer isPassed;

        @ApiModelProperty(value = "指标结论：PASS/FAIL/CONCESSION/WARNING")
        private String indicatorResult;

        @ApiModelProperty(value = "最终判定结论类型")
        private String finalJudgmentType;
    }
}
