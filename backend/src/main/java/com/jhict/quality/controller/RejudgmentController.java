package com.jhict.quality.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jhict.quality.common.entity.ApiResult;
import com.jhict.quality.dto.QcRejudgmentRequestAddCmd;
import com.jhict.quality.dto.RejudgmentApproveCmd;
import com.jhict.quality.entity.QcRejudgmentRequest;
import com.jhict.quality.service.api.RejudgmentService;
import com.jhict.quality.vo.QcRejudgmentRequestVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@SaCheckLogin
@RestController
@RequestMapping("/api/v1/rejudgments")
@Api(tags = "改判管理")
public class RejudgmentController {

    @Resource
    private RejudgmentService rejudgmentService;

    @PostMapping
    @ApiOperation(value = "申请改判")
    public ApiResult<String> apply(@Validated @RequestBody QcRejudgmentRequestAddCmd cmd) {
        return ApiResult.success(rejudgmentService.apply(cmd));
    }

    @PutMapping("/{id}/approve")
    @ApiOperation(value = "审批改判申请")
    public ApiResult<Void> approve(
            @ApiParam(value = "改判申请ID", required = true) @PathVariable String id,
            @Validated @RequestBody RejudgmentApproveCmd cmd) {
        rejudgmentService.approve(id, cmd);
        return ApiResult.success();
    }

    @PostMapping("/page")
    @ApiOperation(value = "分页查询改判申请")
    public ApiResult<IPage<QcRejudgmentRequest>> page(
            @ApiParam(value = "页码") @RequestParam(defaultValue = "1") int pageNum,
            @ApiParam(value = "每页大小") @RequestParam(defaultValue = "10") int pageSize,
            @ApiParam(value = "审批状态") @RequestParam(required = false) String approvalStatus,
            @ApiParam(value = "是否逆向改判（0/1）") @RequestParam(required = false) Integer isReverse) {
        return ApiResult.success(rejudgmentService.page(pageNum, pageSize, approvalStatus, isReverse));
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "查询改判申请详情（含审批历史）")
    public ApiResult<QcRejudgmentRequestVO> getById(
            @ApiParam(value = "改判申请ID", required = true) @PathVariable String id) {
        return ApiResult.success(rejudgmentService.getById(id));
    }
}
