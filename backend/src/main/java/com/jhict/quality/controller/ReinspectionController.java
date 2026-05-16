package com.jhict.quality.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jhict.quality.common.entity.ApiResult;
import com.jhict.quality.dto.QcReinspectionAddCmd;
import com.jhict.quality.dto.ReinspectionCompleteCmd;
import com.jhict.quality.entity.QcReinspectionRecord;
import com.jhict.quality.service.api.ReinspectionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@SaCheckLogin
@RestController
@RequestMapping("/api/v1/reinspections")
@Api(tags = "复检管理")
public class ReinspectionController {

    @Resource
    private ReinspectionService reinspectionService;

    @PostMapping
    @ApiOperation(value = "发起复检申请")
    public ApiResult<String> initiate(@Validated @RequestBody QcReinspectionAddCmd cmd) {
        return ApiResult.success(reinspectionService.initiate(cmd));
    }

    @PutMapping("/{id}/complete")
    @ApiOperation(value = "完成复检（关联新检验记录）")
    public ApiResult<Void> complete(
            @ApiParam(value = "复检记录ID", required = true) @PathVariable String id,
            @Validated @RequestBody ReinspectionCompleteCmd cmd) {
        reinspectionService.complete(id, cmd);
        return ApiResult.success();
    }

    @PostMapping("/page")
    @ApiOperation(value = "分页查询复检记录")
    public ApiResult<IPage<QcReinspectionRecord>> page(
            @ApiParam(value = "页码") @RequestParam(defaultValue = "1") int pageNum,
            @ApiParam(value = "每页大小") @RequestParam(defaultValue = "10") int pageSize,
            @ApiParam(value = "状态（PENDING/COMPLETED）") @RequestParam(required = false) String status,
            @ApiParam(value = "责任人工号") @RequestParam(required = false) String responsibleNo) {
        return ApiResult.success(reinspectionService.page(pageNum, pageSize, status));
    }
}
