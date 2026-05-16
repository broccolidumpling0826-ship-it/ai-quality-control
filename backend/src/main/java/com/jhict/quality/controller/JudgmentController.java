package com.jhict.quality.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jhict.quality.common.entity.ApiResult;
import com.jhict.quality.dto.QcJudgmentPageQuery;
import com.jhict.quality.entity.QcJudgmentResult;
import com.jhict.quality.service.api.JudgmentService;
import com.jhict.quality.vo.DashboardSummaryVO;
import com.jhict.quality.vo.QcJudgmentResultVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/v1/judgments")
@Api(tags = "判定结论管理")
public class JudgmentController {

    @Resource
    private JudgmentService judgmentService;

    @GetMapping("/record/{recordId}")
    @ApiOperation(value = "查询检验记录的当前最终判定结论")
    public ApiResult<QcJudgmentResultVO> getCurrentJudgment(
            @ApiParam(value = "检验记录ID", required = true) @PathVariable String recordId) {
        return ApiResult.success(judgmentService.getCurrentJudgment(recordId));
    }

    @GetMapping("/{id}/explanation")
    @ApiOperation(value = "查询判定结论详情（含完整evidence和优先级匹配过程）")
    public ApiResult<QcJudgmentResultVO> getExplanation(
            @ApiParam(value = "判定结论ID", required = true) @PathVariable String id) {
        return ApiResult.success(judgmentService.getExplanation(id));
    }

    @PostMapping("/page")
    @ApiOperation(value = "分页查询判定结论")
    public ApiResult<IPage<QcJudgmentResult>> page(@RequestBody QcJudgmentPageQuery query) {
        return ApiResult.success(judgmentService.page(query));
    }

    @GetMapping("/dashboard/summary")
    @ApiOperation(value = "获取看板汇总数据（带60s Redis缓存）")
    public ApiResult<DashboardSummaryVO> getDashboardSummary() {
        return ApiResult.success(judgmentService.getDashboardSummary());
    }
}
