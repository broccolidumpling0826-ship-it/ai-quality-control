package com.jhict.quality.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@ApiModel(value = "AiConfidenceConfigUpdateCmd", description = "AI置信度配置更新命令")
public class AiConfidenceConfigUpdateCmd {

    @NotBlank(message = "配置名称不能为空")
    @ApiModelProperty(value = "配置名称", required = true)
    private String configName;

    @NotNull(message = "规则权重不能为空")
    @DecimalMin(value = "0.0000", message = "规则权重不能小于0")
    @DecimalMax(value = "1.0000", message = "规则权重不能大于1")
    @ApiModelProperty(value = "规则权重", required = true)
    private BigDecimal ruleWeight;

    @NotNull(message = "RAG权重不能为空")
    @DecimalMin(value = "0.0000", message = "RAG权重不能小于0")
    @DecimalMax(value = "1.0000", message = "RAG权重不能大于1")
    @ApiModelProperty(value = "RAG权重", required = true)
    private BigDecimal ragWeight;

    @NotNull(message = "LLM权重不能为空")
    @DecimalMin(value = "0.0000", message = "LLM权重不能小于0")
    @DecimalMax(value = "1.0000", message = "LLM权重不能大于1")
    @ApiModelProperty(value = "LLM权重", required = true)
    private BigDecimal llmWeight;

    @NotNull(message = "高置信阈值不能为空")
    @DecimalMin(value = "0.0000", message = "高置信阈值不能小于0")
    @DecimalMax(value = "1.0000", message = "高置信阈值不能大于1")
    @ApiModelProperty(value = "高置信阈值", required = true)
    private BigDecimal highThreshold;

    @NotNull(message = "中置信阈值不能为空")
    @DecimalMin(value = "0.0000", message = "中置信阈值不能小于0")
    @DecimalMax(value = "1.0000", message = "中置信阈值不能大于1")
    @ApiModelProperty(value = "中置信阈值", required = true)
    private BigDecimal mediumThreshold;

    @NotNull(message = "低置信阈值不能为空")
    @DecimalMin(value = "0.0000", message = "低置信阈值不能小于0")
    @DecimalMax(value = "1.0000", message = "低置信阈值不能大于1")
    @ApiModelProperty(value = "低置信阈值", required = true)
    private BigDecimal lowThreshold;

    @ApiModelProperty(value = "是否启用")
    private Integer enabled;

    @ApiModelProperty(value = "备注")
    private String remark;
}
