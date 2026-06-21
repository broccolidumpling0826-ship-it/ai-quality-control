package com.jhict.quality.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jhict.quality.common.entity.CoreEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("qc_ai_call_audit_log")
@ApiModel(value = "QcAiCallAuditLog", description = "AI 调用审计日志")
public class QcAiCallAuditLog extends CoreEntity {

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

    @ApiModelProperty(value = "引用 chunk id JSON")
    private String citationIds;

    @ApiModelProperty(value = "Prompt Token")
    private Integer promptTokens;

    @ApiModelProperty(value = "Completion Token")
    private Integer completionTokens;

    @ApiModelProperty(value = "总 Token")
    private Integer totalTokens;

    @ApiModelProperty(value = "耗时毫秒")
    private Integer latencyMs;

    @ApiModelProperty(value = "是否成功")
    private Integer success;

    @ApiModelProperty(value = "是否降级")
    private Integer degraded;

    @ApiModelProperty(value = "错误类型")
    private String errorType;

    @ApiModelProperty(value = "traceId")
    private String traceId;

    @ApiModelProperty(value = "业务关联ID")
    private String bizRefId;
}
