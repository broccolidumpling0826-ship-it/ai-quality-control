package com.jhict.quality.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jhict.quality.common.entity.ApiResult;
import com.jhict.quality.dto.AiAssessmentHandleCmd;
import com.jhict.quality.dto.AiAssessmentPageQuery;
import com.jhict.quality.entity.QcAiAssessment;
import com.jhict.quality.service.api.AiAssessmentService;
import com.jhict.quality.vo.AiAssessmentVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/v1/ai-assessments")
@Api(tags = "AI评估审计")
public class AiAssessmentController {

    @Resource
    private AiAssessmentService aiAssessmentService;

    @PostMapping("/page")
    @ApiOperation(value = "分页查询AI评估审计")
    @SaCheckPermission("menu:ai-assessment")
    public ApiResult<IPage<AiAssessmentVO>> page(@RequestBody AiAssessmentPageQuery query) {
        return ApiResult.success(aiAssessmentService.page(query));
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "查询AI评估审计详情")
    @SaCheckPermission("menu:ai-assessment")
    public ApiResult<AiAssessmentVO> getById(
            @ApiParam(value = "AI评估ID", required = true) @PathVariable String id) {
        return ApiResult.success(aiAssessmentService.getById(id));
    }

    @PutMapping("/{id}/handle")
    @ApiOperation(value = "采纳或忽略AI评估")
    @SaCheckPermission("ai-assessment:review")
    public ApiResult<QcAiAssessment> handle(
            @ApiParam(value = "AI评估ID", required = true) @PathVariable String id,
            @Validated @RequestBody AiAssessmentHandleCmd cmd) {
        return ApiResult.success(aiAssessmentService.handle(id, cmd));
    }
}
