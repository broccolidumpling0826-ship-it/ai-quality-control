package com.jhict.quality.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@ApiModel(value = "AiAuditDashboardVO", description = "AI 审计看板")
public class AiAuditDashboardVO {

    @ApiModelProperty(value = "总调用次数")
    private Long callCount;

    @ApiModelProperty(value = "总 Token")
    private Long totalTokens;

    @ApiModelProperty(value = "平均耗时毫秒")
    private Double avgLatencyMs;

    @ApiModelProperty(value = "错误率")
    private Double errorRate;

    @ApiModelProperty(value = "按来源统计")
    private List<Map<String, Object>> bySource;

    @ApiModelProperty(value = "缓存更新时间")
    private String cacheUpdatedAt;
}
