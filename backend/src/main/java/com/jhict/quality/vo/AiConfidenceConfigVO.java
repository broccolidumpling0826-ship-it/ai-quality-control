package com.jhict.quality.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@ApiModel(value = "AiConfidenceConfigVO", description = "AI置信度配置视图")
public class AiConfidenceConfigVO {

    @ApiModelProperty(value = "配置ID")
    private String id;

    @ApiModelProperty(value = "配置名称")
    private String configName;

    @ApiModelProperty(value = "规则权重")
    private BigDecimal ruleWeight;

    @ApiModelProperty(value = "RAG权重")
    private BigDecimal ragWeight;

    @ApiModelProperty(value = "LLM权重")
    private BigDecimal llmWeight;

    @ApiModelProperty(value = "高置信阈值")
    private BigDecimal highThreshold;

    @ApiModelProperty(value = "中置信阈值")
    private BigDecimal mediumThreshold;

    @ApiModelProperty(value = "低置信阈值")
    private BigDecimal lowThreshold;

    @ApiModelProperty(value = "是否启用")
    private Integer enabled;

    @ApiModelProperty(value = "是否当前生效")
    private Integer activeFlag;

    @ApiModelProperty(value = "更新人工号")
    private String updatedBy;

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updatedAt;

    @ApiModelProperty(value = "备注")
    private String remark;
}
