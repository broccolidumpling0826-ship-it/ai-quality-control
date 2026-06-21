package com.jhict.quality.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jhict.quality.common.entity.ApiResult;
import com.jhict.quality.dto.QcReinspectionAddCmd;
import com.jhict.quality.dto.QcReinspectionPageQuery;
import com.jhict.quality.dto.ReinspectionCandidateQuery;
import com.jhict.quality.dto.ReinspectionCompleteCmd;
import com.jhict.quality.service.api.ReinspectionService;
import com.jhict.quality.service.api.WorkflowAdviceService;
import com.jhict.quality.vo.InspectionRecordCandidateVO;
import com.jhict.quality.vo.QcReinspectionListVO;
import com.jhict.quality.vo.WorkflowAdviceVO;
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

    @Resource
    private WorkflowAdviceService workflowAdviceService;

    @GetMapping("/advice/{judgmentId}")
    @ApiOperation(value = "生成AI复检建议（不自动创建复检记录）")
    public ApiResult<WorkflowAdviceVO> advice(
            @ApiParam(value = "判定ID", required = true) @PathVariable String judgmentId) {
        return ApiResult.success(workflowAdviceService.adviseReinspection(judgmentId));
    }

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
    public ApiResult<IPage<QcReinspectionListVO>> page(@ModelAttribute QcReinspectionPageQuery query) {
        return ApiResult.success(reinspectionService.page(query));
    }

    @PostMapping("/{id}/candidate-inspections/query")
    @ApiOperation(value = "查询可关联的新检验记录（排除原记录，支持炉号/卷号/批次/客户精确匹配）")
    public ApiResult<IPage<InspectionRecordCandidateVO>> listCandidateInspections(
            @ApiParam(value = "复检记录ID", required = true) @PathVariable String id,
            @RequestBody(required = false) ReinspectionCandidateQuery query) {
        if (query == null) {
            query = new ReinspectionCandidateQuery();
        }
        return ApiResult.success(reinspectionService.listCandidateInspections(id, query));
    }
}
