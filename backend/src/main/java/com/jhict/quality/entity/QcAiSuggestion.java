package com.jhict.quality.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jhict.quality.common.entity.CoreEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("qc_ai_suggestion")
@ApiModel(value = "QcAiSuggestion", description = "复检/改判 AI 建议")
public class QcAiSuggestion extends CoreEntity {

    @ApiModelProperty(value = "建议类型 REINSPECTION/REJUDGMENT")
    private String suggestionType;

    @ApiModelProperty(value = "关联ID")
    private String refId;

    @ApiModelProperty(value = "建议动作 YES/NO/CONDITIONAL")
    private String recommendedAction;

    @ApiModelProperty(value = "关注指标 JSON")
    private String focusIndicators;

    @ApiModelProperty(value = "理由")
    private String reasonText;

    @ApiModelProperty(value = "置信度")
    private String confidenceLevel;

    @ApiModelProperty(value = "是否降级")
    private Integer degraded;

    @ApiModelProperty(value = "审计日志ID")
    private String auditLogId;
}
