package com.jhict.quality.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaCheckRole;
import com.jhict.quality.common.entity.ApiResult;
import com.jhict.quality.dto.AiConfidenceConfigUpdateCmd;
import com.jhict.quality.service.api.AiConfidenceConfigService;
import com.jhict.quality.vo.AiConfidenceConfigVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/v1/ai-confidence")
@Api(tags = "AI置信度配置")
public class AiConfidenceConfigController {

    @Resource
    private AiConfidenceConfigService aiConfidenceConfigService;

    @GetMapping("/active")
    @ApiOperation(value = "查询当前AI置信度配置")
    @SaCheckPermission("menu:ai-confidence")
    public ApiResult<AiConfidenceConfigVO> getActiveConfig() {
        return ApiResult.success(aiConfidenceConfigService.getActiveConfig());
    }

    @PutMapping("/active")
    @ApiOperation(value = "更新当前AI置信度配置")
    @SaCheckRole("ADMIN")
    @SaCheckPermission("ai-confidence:manage")
    public ApiResult<AiConfidenceConfigVO> updateActiveConfig(
            @Validated @RequestBody AiConfidenceConfigUpdateCmd cmd) {
        return ApiResult.success("更新成功", aiConfidenceConfigService.updateActiveConfig(cmd));
    }
}
