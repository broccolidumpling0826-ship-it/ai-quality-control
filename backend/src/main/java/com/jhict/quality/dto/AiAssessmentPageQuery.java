package com.jhict.quality.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "AiAssessmentPageQuery", description = "AI评估审计分页查询")
public class AiAssessmentPageQuery {

    @ApiModelProperty(value = "评估类型")
    private String assessmentType;

    @ApiModelProperty(value = "业务类型")
    private String businessType;

    @ApiModelProperty(value = "业务ID")
    private String businessId;

    @ApiModelProperty(value = "关联判定ID")
    private String relatedJudgmentId;

    @ApiModelProperty(value = "风险等级")
    private String riskLevel;

    @ApiModelProperty(value = "置信度标签")
    private String confidenceLabel;

    @ApiModelProperty(value = "降级来源")
    private String degradationSource;

    @ApiModelProperty(value = "采纳状态 PENDING/ADOPTED/IGNORED")
    private String adoptionStatus;

    @ApiModelProperty(value = "页码，默认1")
    private Integer pageNum = 1;

    @ApiModelProperty(value = "每页条数，默认20")
    private Integer pageSize = 20;
}
