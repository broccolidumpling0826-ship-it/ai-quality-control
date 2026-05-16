package com.jhict.oa.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jhict.common.core.result.ApiResult;
import com.jhict.oa.api.entity.OaLeaveRecord;
import com.jhict.oa.api.query.OaLeaveRecordQuery;
import com.jhict.oa.service.OaLeaveRecordService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 请假记录 Controller
 */
@RestController
@RequestMapping("/oa/leave-record")
@Api(tags = "请假记录管理")
@RequiredArgsConstructor
public class OaLeaveRecordController {

    private final OaLeaveRecordService oaLeaveRecordService;

    @ApiOperation("分页查询请假记录")
    @GetMapping("page")
    public ApiResult page(OaLeaveRecordQuery query) {
        Page<OaLeaveRecord> page = oaLeaveRecordService.page(query);
        return ApiResult.success(page);
    }

    @ApiOperation("查询请假记录列表")
    @GetMapping("list")
    public ApiResult list(OaLeaveRecordQuery query) {
        return ApiResult.success(oaLeaveRecordService.list(query));
    }

    @ApiOperation("获取请假记录详情")
    @GetMapping("get")
    public ApiResult get(String id) {
        return ApiResult.success(oaLeaveRecordService.getById(id));
    }

    @ApiOperation("新增请假记录")
    @PostMapping("save")
    public ApiResult save(@RequestBody OaLeaveRecord entity) {
        oaLeaveRecordService.save(entity);
        return ApiResult.success("新增成功");
    }

    @ApiOperation("更新请假记录")
    @PutMapping("update")
    public ApiResult update(@RequestBody OaLeaveRecord entity) {
        oaLeaveRecordService.updateById(entity);
        return ApiResult.success("更新成功");
    }

    @ApiOperation("删除请假记录")
    @DeleteMapping("remove")
    public ApiResult remove(String id) {
        oaLeaveRecordService.removeById(id);
        return ApiResult.success("删除成功");
    }
}
