package com.jhict.quality.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jhict.quality.common.entity.ApiResult;
import com.jhict.quality.dto.StandardConflictResolveCmd;
import com.jhict.quality.service.api.StandardConflictService;
import com.jhict.quality.vo.StandardConflictVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/standard-conflicts")
@Api(tags = "标准冲突管理")
public class StandardConflictController {

    @Resource
    private StandardConflictService standardConflictService;

    @GetMapping
    @ApiOperation(value = "分页查询标准冲突列表")
    public ApiResult<IPage<StandardConflictVO>> page(
            @ApiParam(value = "页码", defaultValue = "1") @RequestParam(defaultValue = "1") int pageNum,
            @ApiParam(value = "每页条数", defaultValue = "20") @RequestParam(defaultValue = "20") int pageSize,
            @ApiParam(value = "冲突状态（PENDING/RESOLVED/IGNORED）") @RequestParam(required = false) String status) {
        return ApiResult.success(standardConflictService.page(pageNum, pageSize, status));
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "查询标准冲突详情")
    public ApiResult<StandardConflictVO> getById(
            @ApiParam(value = "冲突ID", required = true) @PathVariable String id) {
        return ApiResult.success(standardConflictService.getById(id));
    }

    @PostMapping("/{id}/resolve")
    @ApiOperation(value = "裁定标准冲突（RESOLVED / IGNORED）")
    public ApiResult<Void> resolve(
            @ApiParam(value = "冲突ID", required = true) @PathVariable String id,
            @Valid @RequestBody StandardConflictResolveCmd cmd) {
        String userNo = StpUtil.getLoginIdAsString();
        standardConflictService.resolve(id, cmd, userNo);
        return ApiResult.success(null);
    }
}
