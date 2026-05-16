package com.jhict.quality.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.jhict.quality.common.entity.ApiResult;
import com.jhict.quality.service.api.StatisticsService;
import com.jhict.quality.vo.IndicatorDistributionVO;
import com.jhict.quality.vo.StatisticsOverviewVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@SaCheckLogin
@RestController
@RequestMapping("/api/v1/statistics")
@Api(tags = "质量统计")
public class StatisticsController {

    @Resource
    private StatisticsService statisticsService;

    @GetMapping("/overview")
    @ApiOperation(value = "获取质量统计概览")
    public ApiResult<StatisticsOverviewVO> overview(
            @ApiParam(value = "开始时间（yyyy-MM-dd HH:mm:ss）") @RequestParam(required = false) String timeStart,
            @ApiParam(value = "结束时间（yyyy-MM-dd HH:mm:ss）") @RequestParam(required = false) String timeEnd) {
        return ApiResult.success(statisticsService.getOverview(timeStart, timeEnd));
    }

    @GetMapping("/indicator-distribution")
    @ApiOperation(value = "获取指标不合格分布统计")
    public ApiResult<List<IndicatorDistributionVO>> indicatorDistribution(
            @ApiParam(value = "开始时间（yyyy-MM-dd HH:mm:ss）") @RequestParam(required = false) String timeStart,
            @ApiParam(value = "结束时间（yyyy-MM-dd HH:mm:ss）") @RequestParam(required = false) String timeEnd) {
        return ApiResult.success(statisticsService.getIndicatorDistribution(timeStart, timeEnd));
    }
}
