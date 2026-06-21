package com.jhict.quality.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.jhict.quality.common.entity.ApiResult;
import com.jhict.quality.dto.StandardRagQueryCmd;
import com.jhict.quality.service.api.StandardRagService;
import com.jhict.quality.vo.StandardRagAnswerVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/v1/standard-rag")
@Api(tags = "标准RAG检索")
public class StandardRagController {

    @Resource
    private StandardRagService standardRagService;

    @PostMapping("/query")
    @ApiOperation(value = "自然语言标准检索")
    @SaCheckPermission("standard-rag:query")
    public ApiResult<StandardRagAnswerVO> query(@Validated @RequestBody StandardRagQueryCmd cmd) {
        return ApiResult.success(standardRagService.query(cmd));
    }
}
