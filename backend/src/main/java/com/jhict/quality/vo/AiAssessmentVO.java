package com.jhict.quality.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@ApiModel(value = "AiAssessmentVO", description = "AI评估审计视图")
public class AiAssessmentVO {

    @ApiModelProperty(value = "评估ID")
    private String id;

    @ApiModelProperty(value = "评估类型")
    private String assessmentType;

    @ApiModelProperty(value = "业务类型")
    private String businessType;

    @ApiModelProperty(value = "业务ID")
    private String businessId;

    @ApiModelProperty(value = "关联判定ID")
    private String relatedJudgmentId;

    @ApiModelProperty(value = "输入快照JSON")
    private String inputSnapshot;

    @ApiModelProperty(value = "引用来源JSON")
    private String referencesJson;

    @ApiModelProperty(value = "模型供应商")
    private String modelProvider;

    @ApiModelProperty(value = "模型名称")
    private String modelName;

    @ApiModelProperty(value = "提示词版本")
    private String promptVersion;

    @ApiModelProperty(value = "原始输出")
    private String rawOutput;

    @ApiModelProperty(value = "结构化输出JSON")
    private String structuredOutput;

    @ApiModelProperty(value = "风险等级")
    private String riskLevel;

    @ApiModelProperty(value = "置信度得分")
    private BigDecimal confidenceScore;

    @ApiModelProperty(value = "置信度标签")
    private String confidenceLabel;

    @ApiModelProperty(value = "置信度因素JSON")
    private String confidenceFactors;

    @ApiModelProperty(value = "降级来源")
    private String degradationSource;

    @ApiModelProperty(value = "是否命中缓存")
    private Integer cacheHit;

    @ApiModelProperty(value = "缓存键")
    private String cacheKey;

    @ApiModelProperty(value = "采纳状态")
    private String adoptionStatus;

    @ApiModelProperty(value = "人工意见")
    private String humanOpinion;

    @ApiModelProperty(value = "处理人工号")
    private String handledBy;

    @ApiModelProperty(value = "处理时间")
    private LocalDateTime handledTime;

    @ApiModelProperty(value = "创建人工号")
    private String createUserNo;

    @ApiModelProperty(value = "创建时间")
    private String createDateTime;
}
