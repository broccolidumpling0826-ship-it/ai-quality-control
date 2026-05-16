package com.jhict.quality.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@ApiModel(value = "QcJudgmentResultVO", description = "判定结论视图对象")
public class QcJudgmentResultVO {

    @ApiModelProperty(value = "判定结论ID")
    private String judgmentId;

    @ApiModelProperty(value = "检验记录ID")
    private String recordId;

    @ApiModelProperty(value = "卷号")
    private String coilNo;

    @ApiModelProperty(value = "判定结论类型")
    private String judgmentType;

    @ApiModelProperty(value = "判定时间（yyyy-MM-dd HH:mm:ss）")
    private String judgmentTime;

    @ApiModelProperty(value = "是否最终结论：1是 0否")
    private Integer isFinal;

    @ApiModelProperty(value = "命中的标准列表")
    private List<MatchedStandardVO> matchedStandards;

    @ApiModelProperty(value = "判定依据列表")
    private List<EvidenceVO> evidences;

    @Data
    @ApiModel(value = "MatchedStandardVO", description = "命中标准信息")
    public static class MatchedStandardVO {

        @ApiModelProperty(value = "标准ID")
        private String standardId;

        @ApiModelProperty(value = "标准类型")
        private String standardType;

        @ApiModelProperty(value = "版本号")
        private String versionNo;
    }

    @Data
    @ApiModel(value = "EvidenceVO", description = "判定依据详情")
    public static class EvidenceVO {

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

        @ApiModelProperty(value = "偏差值")
        private BigDecimal deviation;

        @ApiModelProperty(value = "触发规则描述")
        private String triggerRule;

        @ApiModelProperty(value = "是否通过：1通过 0未通过")
        private Integer isPassed;
    }
}
