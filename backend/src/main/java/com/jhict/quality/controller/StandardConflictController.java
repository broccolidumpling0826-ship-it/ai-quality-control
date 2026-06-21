package com.jhict.quality.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jhict.quality.common.entity.ApiResult;
import com.jhict.quality.dto.StandardConflictPageQuery;
import com.jhict.quality.dto.StandardConflictResolveCmd;
import com.jhict.quality.service.api.StandardConflictService;
import com.jhict.quality.service.api.StandardConflictWorkflowService;
import com.jhict.quality.vo.StandardConflictVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/v1/standard-conflicts")
@Api(tags = "标准冲突检测")
public class StandardConflictController {

    @Resource
    private StandardConflictService standardConflictService;

    @Resource
    private StandardConflictWorkflowService standardConflictWorkflowService;

    @PostMapping("/page")
    @ApiOperation(value = "分页查询标准冲突")
    @SaCheckPermission("menu:standard-conflict")
    public ApiResult<IPage<StandardConflictVO>> page(@RequestBody StandardConflictPageQuery query) {
        return ApiResult.success(standardConflictService.page(query));
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "查询标准冲突详情")
    @SaCheckPermission("menu:standard-conflict")
    public ApiResult<StandardConflictVO> getById(
            @ApiParam(value = "冲突ID", required = true) @PathVariable String id) {
        return ApiResult.success(standardConflictService.getById(id));
    }

    @PostMapping("/{id}/resolve")
    @ApiOperation(value = "裁决标准冲突并重新判定")
    @SaCheckPermission("standard-conflict:resolve")
    public ApiResult<StandardConflictVO> resolve(
            @ApiParam(value = "冲突ID", required = true) @PathVariable String id,
            @Validated @RequestBody StandardConflictResolveCmd cmd) {
        return ApiResult.success("裁决成功", standardConflictWorkflowService.resolveAndRejudge(id, cmd));
    }
}
