package com.jhict.quality.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jhict.quality.common.entity.ApiResult;
import com.jhict.quality.dto.QcIndicatorItemAddCmd;
import com.jhict.quality.dto.QcIndicatorItemPageQuery;
import com.jhict.quality.service.api.IndicatorService;
import com.jhict.quality.vo.QcIndicatorItemVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/indicators")
@Api(tags = "指标项目管理")
public class IndicatorController {

    @Resource
    private IndicatorService indicatorService;

    @PostMapping("/page")
    @ApiOperation(value = "分页查询指标项目")
    public ApiResult<IPage<QcIndicatorItemVO>> page(@RequestBody QcIndicatorItemPageQuery query) {
        return ApiResult.success(indicatorService.page(query));
    }

    @PostMapping
    @ApiOperation(value = "新增指标项目")
    @SaCheckRole("QUALITY_ENGINEER")
    public ApiResult<String> add(@Validated @RequestBody QcIndicatorItemAddCmd cmd) {
        String id = indicatorService.add(cmd);
        return ApiResult.success("新增成功", id);
    }

    @PutMapping("/{id}")
    @ApiOperation(value = "更新指标项目")
    @SaCheckRole("QUALITY_ENGINEER")
    public ApiResult<Void> update(
            @ApiParam(value = "指标ID", required = true) @PathVariable String id,
            @Validated @RequestBody QcIndicatorItemAddCmd cmd) {
        indicatorService.update(id, cmd);
        return ApiResult.success();
    }

    @PutMapping("/{id}/status")
    @ApiOperation(value = "启用/停用指标项目")
    @SaCheckRole("QUALITY_ENGINEER")
    public ApiResult<Void> updateStatus(
            @ApiParam(value = "指标ID", required = true) @PathVariable String id,
            @RequestBody Map<String, String> body) {
        indicatorService.updateStatus(id, body.get("status"));
        return ApiResult.success();
    }
}
