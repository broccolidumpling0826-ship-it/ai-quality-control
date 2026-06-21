package com.jhict.quality.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "StatisticsOverviewVO", description = "质量统计概览视图对象")
public class StatisticsOverviewVO {

    @ApiModelProperty(value = "检验总数")
    private long totalInspection;

    @ApiModelProperty(value = "合格数量")
    private long qualifiedCount;

    @ApiModelProperty(value = "不合格数量")
    private long unqualifiedCount;

    @ApiModelProperty(value = "需复检数量")
    private long needReinspectionCount;

    @ApiModelProperty(value = "可让步数量")
    private long canConcessionCount;

    @ApiModelProperty(value = "不合格率（百分比字符串，如\"12.34%\"）")
    private String unqualifiedRate;

    @ApiModelProperty(value = "复检率（百分比字符串）")
    private String reinspectionRate;

    @ApiModelProperty(value = "让步率（百分比字符串）")
    private String concessionRate;

    @ApiModelProperty(value = "标准冲突数量")
    private long standardConflictCount;

    @ApiModelProperty(value = "标准冲突率（百分比字符串）")
    private String standardConflictRate;

    @ApiModelProperty(value = "低置信AI输出数量")
    private long lowConfidenceAiCount;

    @ApiModelProperty(value = "AI评估输出总数")
    private long aiAssessmentCount;

    @ApiModelProperty(value = "AI评估采纳数量")
    private long aiAdoptedCount;

    @ApiModelProperty(value = "AI采纳率（百分比字符串）")
    private String aiAdoptionRate;

    @ApiModelProperty(value = "AI命中率（采纳/已处理，百分比字符串）")
    private String aiHitRate;

    @ApiModelProperty(value = "AI人工复核处理率（已处理/总数，百分比字符串）")
    private String manualReviewHandleRate;

    @ApiModelProperty(value = "标准冲突已裁决数量")
    private long standardConflictResolvedCount;

    @ApiModelProperty(value = "标准冲突裁决闭环率（百分比字符串）")
    private String conflictRemediationRate;

    @ApiModelProperty(value = "AI与冲突治理趋势摘要")
    private String analyticsTrendSummary;

    @ApiModelProperty(value = "评测集样例数量")
    private long evaluationCaseCount;

    @ApiModelProperty(value = "评测摘要")
    private String evaluationSummary;
}
