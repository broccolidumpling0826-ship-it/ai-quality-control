package com.jhict.quality.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jhict.quality.common.entity.ApiResult;
import com.jhict.quality.dto.QcInspectionRecordAddCmd;
import com.jhict.quality.dto.QcInspectionRecordPageQuery;
import com.jhict.quality.entity.QcInspectionRecord;
import com.jhict.quality.service.api.InspectionService;
import com.jhict.quality.vo.InspectionResultVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/inspections")
@Api(tags = "检验记录管理")
public class InspectionController {

    @Resource
    private InspectionService inspectionService;

    @PostMapping
    @ApiOperation(value = "新增检验记录（自动触发判定）")
    public ApiResult<InspectionResultVO> addRecord(@Validated @RequestBody QcInspectionRecordAddCmd cmd) {
        return ApiResult.success(inspectionService.addRecord(cmd));
    }

    @PutMapping("/{id}/void")
    @ApiOperation(value = "作废检验记录")
    public ApiResult<Void> voidRecord(
            @ApiParam(value = "检验记录ID", required = true) @PathVariable String id,
            @RequestBody Map<String, String> body) {
        String reason = body.get("reason");
        inspectionService.voidRecord(id, reason);
        return ApiResult.success();
    }

    @PostMapping("/page")
    @ApiOperation(value = "分页查询检验记录")
    public ApiResult<IPage<QcInspectionRecord>> page(@RequestBody QcInspectionRecordPageQuery query) {
        return ApiResult.success(inspectionService.page(query));
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "查询检验记录详情（含检验值列表）")
    public ApiResult<Map<String, Object>> getById(
            @ApiParam(value = "检验记录ID", required = true) @PathVariable String id) {
        return ApiResult.success(inspectionService.getById(id));
    }
}
