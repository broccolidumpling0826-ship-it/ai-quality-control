package com.jhict.quality.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel(value = "DashboardSummaryVO", description = "看板汇总数据视图对象")
public class DashboardSummaryVO {

    @ApiModelProperty(value = "待判定数量")
    private Long pendingJudgmentCount;

    @ApiModelProperty(value = "不合格数量")
    private Long unqualifiedCount;

    @ApiModelProperty(value = "待复检数量")
    private Long pendingReinspectionCount;

    @ApiModelProperty(value = "待让步审批数量")
    private Long pendingConcessionApprovalCount;

    @ApiModelProperty(value = "缓存更新时间")
    private String cacheUpdatedAt;

    @ApiModelProperty(value = "决赛演示快捷入口")
    private List<DemoScenarioVO> demoLinks;

    @ApiModelProperty(value = "AI 风险预警数量")
    private Long aiRiskAlertCount;
}
