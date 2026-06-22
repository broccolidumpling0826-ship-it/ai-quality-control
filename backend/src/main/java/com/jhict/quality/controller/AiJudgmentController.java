package com.jhict.quality.controller;

import com.jhict.quality.common.entity.ApiResult;
import com.jhict.quality.service.api.AiJudgmentExplainService;
import com.jhict.quality.vo.AiJudgmentExplanationVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * AI 判定解释控制器
 * 提供判定结论的自然语言解释、置信度与 RAG 引用
 */
@RestController
@RequestMapping("/api/v1/judgments")
@Api(tags = "AI 判定解释")
public class AiJudgmentController {

    @Resource
    private AiJudgmentExplainService aiJudgmentExplainService;

    @GetMapping("/{id}/ai-explanation")
    @ApiOperation(value = "获取判定结论 AI 解释（含置信度、RAG引用、降级标记）",
            notes = "调用 LlmClient 双轨：AI 轨（RAG+LLM）优先，失败降级为规则基线摘要")
    public ApiResult<AiJudgmentExplanationVO> getAiExplanation(
            @ApiParam(value = "判定结论ID", required = true) @PathVariable String id) {
        return ApiResult.success(aiJudgmentExplainService.explain(id));
    }

    @GetMapping("/demo/{code}/ai-explanation")
    @ApiOperation(value = "获取演示批次 AI 解释（支持四场景演示码）",
            notes = "demoCode 支持：QUALIFIED / UNQUALIFIED / CONCESSION / CONFLICT 或完整批次号")
    public ApiResult<AiJudgmentExplanationVO> getDemoAiExplanation(
            @ApiParam(value = "演示批次码，如 QUALIFIED / DEMO-QUALIFIED-001", required = true)
            @PathVariable String code) {
        return ApiResult.success(aiJudgmentExplainService.explainByDemoCode(code));
    }
}
