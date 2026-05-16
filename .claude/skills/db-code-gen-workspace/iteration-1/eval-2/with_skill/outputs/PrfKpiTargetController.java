package com.jhict.performance.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jhict.common.core.result.ApiResult;
import com.jhict.performance.api.entity.PrfKpiTarget;
import com.jhict.performance.api.query.PrfKpiTargetQuery;
import com.jhict.performance.service.PrfKpiTargetService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * KPI目标设置 Controller
 */
@RestController
@RequestMapping("/performance/prf-kpi-target")
@Api(tags = "KPI目标设置管理")
@RequiredArgsConstructor
public class PrfKpiTargetController {

    private final PrfKpiTargetService prfKpiTargetService;

    @ApiOperation("分页查询KPI目标设置")
    @GetMapping("page")
    public ApiResult page(PrfKpiTargetQuery query) {
        Page<PrfKpiTarget> page = prfKpiTargetService.page(query);
        return ApiResult.success(page);
    }

    @ApiOperation("查询KPI目标设置列表")
    @GetMapping("list")
    public ApiResult list(PrfKpiTargetQuery query) {
        return ApiResult.success(prfKpiTargetService.list(query));
    }

    @ApiOperation("获取KPI目标设置详情")
    @GetMapping("get")
    public ApiResult get(String id) {
        return ApiResult.success(prfKpiTargetService.getById(id));
    }

    @ApiOperation("新增KPI目标设置")
    @PostMapping("save")
    public ApiResult save(@RequestBody PrfKpiTarget entity) {
        prfKpiTargetService.save(entity);
        return ApiResult.success("新增成功");
    }

    @ApiOperation("更新KPI目标设置")
    @PutMapping("update")
    public ApiResult update(@RequestBody PrfKpiTarget entity) {
        prfKpiTargetService.updateById(entity);
        return ApiResult.success("更新成功");
    }

    @ApiOperation("删除KPI目标设置")
    @DeleteMapping("remove")
    public ApiResult remove(String id) {
        prfKpiTargetService.removeById(id);
        return ApiResult.success("删除成功");
    }
}
