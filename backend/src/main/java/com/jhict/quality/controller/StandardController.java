package com.jhict.quality.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jhict.quality.common.entity.ApiResult;
import com.jhict.quality.dto.QcQualityStandardAddCmd;
import com.jhict.quality.dto.QcQualityStandardPageQuery;
import com.jhict.quality.service.api.StandardService;
import com.jhict.quality.vo.QcIndicatorItemVO;
import com.jhict.quality.vo.QcQualityStandardDetailVO;
import com.jhict.quality.vo.QcQualityStandardVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/standards")
@Api(tags = "质量标准管理")
public class StandardController {

    @Resource
    private StandardService standardService;

    @PostMapping
    @ApiOperation(value = "新增质量标准")
    @SaCheckRole("QUALITY_ENGINEER")
    public ApiResult<String> addStandard(@Validated @RequestBody QcQualityStandardAddCmd cmd) {
        String id = standardService.addStandard(cmd);
        return ApiResult.success("新增成功", id);
    }

    @PutMapping("/{id}")
    @ApiOperation(value = "更新质量标准（仅DRAFT状态可更新）")
    public ApiResult<Void> updateStandard(
            @ApiParam(value = "标准ID", required = true) @PathVariable String id,
            @Validated @RequestBody QcQualityStandardAddCmd cmd) {
        cmd.setId(id);
        standardService.updateStandard(cmd);
        return ApiResult.success();
    }

    @PutMapping("/{id}/publish")
    @ApiOperation(value = "发布质量标准")
    public ApiResult<Map<String, Object>> publishStandard(
            @ApiParam(value = "标准ID", required = true) @PathVariable String id) {
        Map<String, Object> result = standardService.publishStandard(id);
        return ApiResult.success(result);
    }

    @PostMapping("/page")
    @ApiOperation(value = "分页查询质量标准")
    public ApiResult<IPage<QcQualityStandardVO>> page(@RequestBody QcQualityStandardPageQuery query) {
        return ApiResult.success(standardService.page(query));
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "查询质量标准详情（含指标配置）")
    public ApiResult<QcQualityStandardDetailVO> getById(
            @ApiParam(value = "标准ID", required = true) @PathVariable String id) {
        return ApiResult.success(standardService.getById(id));
    }

    @GetMapping("/indicators")
    @ApiOperation(value = "查询可用指标列表（用于标准配置时指标选择）")
    public ApiResult<List<QcIndicatorItemVO>> listIndicators(
            @ApiParam(value = "关键词（指标名称/代码）") @RequestParam(required = false) String keyword,
            @ApiParam(value = "指标类别") @RequestParam(required = false) String category) {
        return ApiResult.success(standardService.listIndicators(keyword, category));
    }
}
