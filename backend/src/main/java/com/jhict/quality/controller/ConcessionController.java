package com.jhict.quality.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jhict.quality.common.entity.ApiResult;
import com.jhict.quality.dto.ConcessionConfirmCmd;
import com.jhict.quality.dto.QcConcessionAddCmd;
import com.jhict.quality.dto.QcConcessionPageQuery;
import com.jhict.quality.service.api.ConcessionService;
import com.jhict.quality.vo.QcConcessionVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

@SaCheckLogin
@RestController
@RequestMapping("/api/v1/concessions")
@Api(tags = "让步接收管理")
public class ConcessionController {

    @Resource
    private ConcessionService concessionService;

    @PostMapping
    @ApiOperation(value = "发起让步接收申请")
    public ApiResult<String> apply(@Validated @RequestBody QcConcessionAddCmd cmd) {
        return ApiResult.success(concessionService.apply(cmd));
    }

    @PutMapping("/{id}/confirm")
    @ApiOperation(value = "客户确认让步（上传确认附件）")
    public ApiResult<Void> confirm(
            @ApiParam(value = "让步接收申请ID", required = true) @PathVariable String id,
            @RequestPart(value = "file") MultipartFile file,
            @RequestPart(value = "cmd", required = false) ConcessionConfirmCmd cmd) {
        concessionService.confirm(id, cmd, file);
        return ApiResult.success();
    }

    @PutMapping("/{id}/reject")
    @ApiOperation(value = "拒绝让步接收")
    public ApiResult<Void> reject(
            @ApiParam(value = "让步接收申请ID", required = true) @PathVariable String id,
            @ApiParam(value = "拒绝备注") @RequestParam(required = false) String rejectNote,
            @RequestPart(value = "rejectFile", required = false) MultipartFile rejectFile) {
        concessionService.reject(id, rejectNote, rejectFile);
        return ApiResult.success();
    }

    @PutMapping("/{id}/approve")
    @ApiOperation(value = "审批通过让步接收")
    public ApiResult<Void> approve(
            @ApiParam(value = "让步接收申请ID", required = true) @PathVariable String id,
            @ApiParam(value = "审批意见") @RequestParam(required = false) String comment) {
        String role = StpUtil.getExtra("role") != null ? StpUtil.getExtra("role").toString() : "";
        concessionService.approve(id, comment, role);
        return ApiResult.success();
    }

    @PostMapping("/page")
    @ApiOperation(value = "分页查询让步接收申请")
    public ApiResult<IPage<QcConcessionVO>> page(@ModelAttribute QcConcessionPageQuery query) {
        return ApiResult.success(concessionService.page(query));
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "查询让步接收申请详情")
    public ApiResult<QcConcessionVO> getById(
            @ApiParam(value = "让步接收申请ID", required = true) @PathVariable String id) {
        return ApiResult.success(concessionService.getById(id));
    }
}
