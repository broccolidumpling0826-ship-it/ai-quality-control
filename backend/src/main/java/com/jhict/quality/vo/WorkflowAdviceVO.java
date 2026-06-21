package com.jhict.quality.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel(value = "WorkflowAdviceVO", description = "AI流程建议视图")
public class WorkflowAdviceVO {

    @ApiModelProperty(value = "AI评估记录ID")
    private String assessmentId;

    @ApiModelProperty(value = "建议类型 REINSPECTION_ADVICE/REJUDGMENT_ADVICE")
    private String adviceType;

    @ApiModelProperty(value = "判定ID")
    private String judgmentId;

    @ApiModelProperty(value = "检验记录ID")
    private String recordId;

    @ApiModelProperty(value = "建议动作")
    private String recommendedAction;

    @ApiModelProperty(value = "是否隐藏确定性建议")
    private Boolean withheld;

    @ApiModelProperty(value = "是否必须人工复核")
    private Boolean mustManualReview;

    @ApiModelProperty(value = "目标判定类型")
    private String targetJudgmentType;

    @ApiModelProperty(value = "建议原因")
    private String suggestedReason;

    @ApiModelProperty(value = "影响范围")
    private String affectedScope;

    @ApiModelProperty(value = "证据摘要")
    private String evidenceSummary;

    @ApiModelProperty(value = "置信度标签")
    private String confidenceLabel;

    @ApiModelProperty(value = "置信度分数")
    private Double confidenceScore;

    @ApiModelProperty(value = "缺失信息")
    private List<String> missingInfo;

    @ApiModelProperty(value = "触发指标")
    private List<String> triggerIndicators;

    @ApiModelProperty(value = "引用来源")
    private List<AiSourceReferenceVO> evidenceRefs;
}
