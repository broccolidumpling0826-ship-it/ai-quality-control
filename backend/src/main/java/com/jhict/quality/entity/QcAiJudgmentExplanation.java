package com.jhict.quality.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jhict.quality.common.entity.CoreEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("qc_ai_judgment_explanation")
@ApiModel(value = "QcAiJudgmentExplanation", description = "AI 判定解释")
public class QcAiJudgmentExplanation extends CoreEntity {

    @ApiModelProperty(value = "判定ID")
    private String judgmentId;

    @ApiModelProperty(value = "自然语言解释")
    private String narrativeText;

    @ApiModelProperty(value = "置信度")
    private String confidenceLevel;

    @ApiModelProperty(value = "是否需人工复核")
    private Integer manualReviewRequired;

    @ApiModelProperty(value = "是否降级")
    private Integer degraded;

    @ApiModelProperty(value = "规则基线 JSON")
    private String baselineJson;

    @ApiModelProperty(value = "引用 chunk ids")
    private String citationIds;

    @ApiModelProperty(value = "审计日志ID")
    private String auditLogId;
}
