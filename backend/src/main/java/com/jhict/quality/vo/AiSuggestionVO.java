package com.jhict.quality.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@ApiModel(value = "AiSuggestionVO", description = "复检/改判 AI 建议")
public class AiSuggestionVO {

    @ApiModelProperty(value = "建议ID")
    private String id;

    @ApiModelProperty(value = "建议类型")
    private String suggestionType;

    @ApiModelProperty(value = "关联判定ID")
    private String refId;

    @ApiModelProperty(value = "建议动作 YES/NO/CONDITIONAL")
    private String recommendedAction;

    @ApiModelProperty(value = "关注指标")
    private List<String> focusIndicators;

    @ApiModelProperty(value = "理由")
    private String reasonText;

    @ApiModelProperty(value = "置信度")
    private String confidenceLevel;

    @ApiModelProperty(value = "是否降级")
    private Boolean degraded;

    @ApiModelProperty(value = "审计日志ID")
    private String auditLogId;
}
