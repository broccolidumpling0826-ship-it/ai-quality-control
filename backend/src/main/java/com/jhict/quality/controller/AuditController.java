package com.jhict.quality.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jhict.quality.common.entity.ApiResult;
import com.jhict.quality.entity.QcAuditLog;
import com.jhict.quality.mapper.QcAuditLogMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@SaCheckLogin
@RestController
@RequestMapping("/api/v1/audit-logs")
@Api(tags = "审计日志")
public class AuditController {

    @Resource
    private QcAuditLogMapper auditLogMapper;

    @PostMapping("/page")
    @ApiOperation(value = "分页查询审计日志")
    public ApiResult<IPage<QcAuditLog>> page(
            @ApiParam(value = "页码") @RequestParam(defaultValue = "1") int pageNum,
            @ApiParam(value = "每页大小") @RequestParam(defaultValue = "10") int pageSize,
            @ApiParam(value = "操作类型") @RequestParam(required = false) String operationType,
            @ApiParam(value = "操作人工号") @RequestParam(required = false) String operatorNo,
            @ApiParam(value = "开始时间（yyyy-MM-dd HH:mm:ss）") @RequestParam(required = false) String timeStart,
            @ApiParam(value = "结束时间（yyyy-MM-dd HH:mm:ss）") @RequestParam(required = false) String timeEnd) {

        LambdaQueryWrapper<QcAuditLog> wrapper = new LambdaQueryWrapper<QcAuditLog>()
                .orderByDesc(QcAuditLog::getOperateTime);

        if (StringUtils.hasText(operationType)) {
            wrapper.eq(QcAuditLog::getOperationType, operationType);
        }
        if (StringUtils.hasText(operatorNo)) {
            wrapper.eq(QcAuditLog::getOperatorNo, operatorNo);
        }
        if (StringUtils.hasText(timeStart)) {
            wrapper.ge(QcAuditLog::getOperateTime, timeStart);
        }
        if (StringUtils.hasText(timeEnd)) {
            wrapper.le(QcAuditLog::getOperateTime, timeEnd);
        }

        IPage<QcAuditLog> result = auditLogMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return ApiResult.success(result);
    }
}
