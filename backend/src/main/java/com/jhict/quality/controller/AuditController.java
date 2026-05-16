package com.jhict.quality.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jhict.quality.common.entity.ApiResult;
import com.jhict.quality.dto.QcAuditLogPageQuery;
import com.jhict.quality.entity.QcAuditLog;
import com.jhict.quality.service.api.AuditLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@SaCheckLogin
@RestController
@RequestMapping("/api/v1/audit-logs")
@Api(tags = "审计日志")
public class AuditController {

    @Resource
    private AuditLogService auditLogService;

    @PostMapping("/page")
    @ApiOperation(value = "分页查询审计日志")
    public ApiResult<IPage<QcAuditLog>> page(@RequestBody QcAuditLogPageQuery query) {
        return ApiResult.success(auditLogService.pageAuditLogs(query));
    }
}
