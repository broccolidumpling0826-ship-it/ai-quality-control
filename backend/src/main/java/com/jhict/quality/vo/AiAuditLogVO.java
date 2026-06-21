package com.jhict.quality.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "AiAuditLogVO", description = "AI 审计日志回放")
public class AiAuditLogVO {

    @ApiModelProperty(value = "审计ID")
    private String id;

    @ApiModelProperty(value = "调用来源")
    private String callSource;

    @ApiModelProperty(value = "Prompt 键")
    private String promptKey;

    @ApiModelProperty(value = "Prompt 版本")
    private String promptVersion;

    @ApiModelProperty(value = "模型名称")
    private String modelName;

    @ApiModelProperty(value = "脱敏输入摘要")
    private String inputSummary;

    @ApiModelProperty(value = "脱敏输出摘要")
    private String outputSummary;

    @ApiModelProperty(value = "引用 IDs")
    private String citationIds;

    @ApiModelProperty(value = "总 Token")
    private Integer totalTokens;

    @ApiModelProperty(value = "耗时毫秒")
    private Integer latencyMs;

    @ApiModelProperty(value = "是否成功")
    private Boolean success;

    @ApiModelProperty(value = "是否降级")
    private Boolean degraded;

    @ApiModelProperty(value = "错误类型")
    private String errorType;

    @ApiModelProperty(value = "traceId")
    private String traceId;

    @ApiModelProperty(value = "创建时间")
    private String createDateTime;
}
