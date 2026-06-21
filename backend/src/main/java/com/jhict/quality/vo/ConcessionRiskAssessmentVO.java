package com.jhict.quality.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel(value = "ConcessionRiskAssessmentVO", description = "让步风险评估结果")
public class ConcessionRiskAssessmentVO {

    @ApiModelProperty(value = "判定ID")
    private String judgmentId;

    @ApiModelProperty(value = "风险等级 LOW/MEDIUM/HIGH/BLOCKED")
    private String riskLevel;

    @ApiModelProperty(value = "是否必须人工复核")
    private Boolean mustReview;

    @ApiModelProperty(value = "缺失信息")
    private List<String> missingInfo;

    @ApiModelProperty(value = "建议条件")
    private List<String> suggestedConditions;

    @ApiModelProperty(value = "阻断原因")
    private List<String> blockingReasons;

    @ApiModelProperty(value = "证据引用")
    private List<AiSourceReferenceVO> evidenceRefs;

    @ApiModelProperty(value = "替代库存")
    private List<AlternativeStockVO> alternativeStocks;

    @ApiModelProperty(value = "客户用途")
    private String customerUsage;

    @ApiModelProperty(value = "用途风险类别")
    private String usageRiskCategory;

    @ApiModelProperty(value = "置信度标签")
    private String confidenceLabel;

    @ApiModelProperty(value = "置信度分值")
    private Double confidenceScore;

    @ApiModelProperty(value = "降级来源")
    private String degradationSource;

    @ApiModelProperty(value = "说明文本")
    private String narrativeExplanation;

    @ApiModelProperty(value = "AI评估记录ID")
    private String assessmentId;
}
