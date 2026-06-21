package com.jhict.quality.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.jhict.quality.common.entity.ApiResult;
import com.jhict.quality.service.api.DashboardService;
import com.jhict.quality.vo.DashboardMessageVO;
import com.jhict.quality.vo.DashboardPendingItemVO;
import com.jhict.quality.vo.DashboardSummaryVO;
import com.jhict.quality.vo.DemoScenarioVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/v1/dashboard")
@Api(tags = "质量工作台")
public class DashboardController {

    @Resource
    private DashboardService dashboardService;

    @GetMapping("/pending-items")
    @ApiOperation(value = "待处理事项列表")
    public ApiResult<List<DashboardPendingItemVO>> pendingItems() {
        String userNo = StpUtil.getLoginIdAsString();
        return ApiResult.success(dashboardService.listPendingItems(userNo));
    }

    @GetMapping("/messages")
    @ApiOperation(value = "站内消息列表（工作台展示）")
    public ApiResult<List<DashboardMessageVO>> messages() {
        String userNo = StpUtil.getLoginIdAsString();
        return ApiResult.success(dashboardService.listMessages(userNo));
    }

    @GetMapping("/overview")
    @ApiOperation(value = "看板汇总（含 AI 风险预警 + Demo 入口）")
    public ApiResult<DashboardSummaryVO> overview() {
        return ApiResult.success(dashboardService.getOverview());
    }

    @GetMapping("/demo-scenarios")
    @ApiOperation(value = "决赛演示快捷入口列表")
    public ApiResult<List<DemoScenarioVO>> demoScenarios() {
        return ApiResult.success(dashboardService.listDemoScenarios());
    }
}
