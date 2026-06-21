package com.jhict.quality.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel(value = "AiJudgmentExplanationVO", description = "AI 判定解释")
public class AiJudgmentExplanationVO {

    @ApiModelProperty(value = "判定ID")
    private String judgmentId;

    @ApiModelProperty(value = "自然语言解释")
    private String narrativeText;

    @ApiModelProperty(value = "置信度 HIGH/MEDIUM/LOW")
    private String confidenceLevel;

    @ApiModelProperty(value = "是否需人工复核")
    private Boolean manualReviewRequired;

    @ApiModelProperty(value = "是否降级生成")
    private Boolean degraded;

    @ApiModelProperty(value = "规则基线 JSON")
    private String baselineJson;

    @ApiModelProperty(value = "引用列表")
    private List<CitationVO> citations;

    @ApiModelProperty(value = "审计日志ID")
    private String auditLogId;
}
