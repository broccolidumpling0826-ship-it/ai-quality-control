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

    @ApiModelProperty(value = "批次号")
    private String batchNo;

    @ApiModelProperty(value = "炉号")
    private String heatNo;

    @ApiModelProperty(value = "品种")
    private String productVariety;

    @ApiModelProperty(value = "牌号")
    private String productGrade;

    @ApiModelProperty(value = "规格")
    private String productSpec;

    @ApiModelProperty(value = "规格（前端别名 specification）")
    private String specification;

    @ApiModelProperty(value = "检验人工号")
    private String testerNo;

    @ApiModelProperty(value = "检验人（前端展示名）")
    private String inspector;

    @ApiModelProperty(value = "判定时间（前端别名 judgeTime）")
    private String judgeTime;

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

    @ApiModelProperty(value = "标准优先级匹配过程（前端 standardMatches）")
    private List<StandardMatchVO> standardMatches;

    @ApiModelProperty(value = "指标明细（前端 indicatorDetails，与 evidences 同源）")
    private List<IndicatorDetailVO> indicatorDetails;

    @Data
    @ApiModel(value = "StandardMatchVO", description = "标准优先级匹配卡片")
    public static class StandardMatchVO {

        @ApiModelProperty(value = "标准类型")
        private String standardType;

        @ApiModelProperty(value = "是否命中")
        private Boolean hit;

        @ApiModelProperty(value = "标准展示名")
        private String standardName;

        @ApiModelProperty(value = "跳过原因")
        private String skipReason;
    }

    @Data
    @ApiModel(value = "IndicatorDetailVO", description = "指标明细（解释页表格）")
    public static class IndicatorDetailVO {

        @ApiModelProperty(value = "指标名称")
        private String indicatorName;

        @ApiModelProperty(value = "实测值")
        private BigDecimal measuredValue;

        @ApiModelProperty(value = "标准下限")
        private BigDecimal lowerLimit;

        @ApiModelProperty(value = "标准上限")
        private BigDecimal upperLimit;

        @ApiModelProperty(value = "让步下限")
        private BigDecimal concessionLower;

        @ApiModelProperty(value = "让步上限")
        private BigDecimal concessionUpper;

        @ApiModelProperty(value = "偏差值")
        private BigDecimal deviation;

        @ApiModelProperty(value = "触发规则")
        private String triggeredRule;

        @ApiModelProperty(value = "指标结论（PASS/FAIL/WARNING）")
        private String indicatorResult;

        @ApiModelProperty(value = "是否无标准覆盖")
        private Boolean noStandard;
    }

    @Data
    @ApiModel(value = "MatchedStandardVO", description = "命中标准信息")
    public static class MatchedStandardVO {

        @ApiModelProperty(value = "标准ID")
        private String standardId;

        @ApiModelProperty(value = "标准类型")
        private String standardType;

        @ApiModelProperty(value = "版本号")
        private String versionNo;

        @ApiModelProperty(value = "规格范围")
        private String specRange;
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
