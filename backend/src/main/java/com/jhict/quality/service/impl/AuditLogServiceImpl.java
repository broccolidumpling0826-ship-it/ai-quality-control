package com.jhict.quality.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jhict.quality.dto.QcAuditLogPageQuery;
import com.jhict.quality.entity.QcAuditLog;
import com.jhict.quality.mapper.QcAuditLogMapper;
import com.jhict.quality.service.api.AuditLogService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Service
public class AuditLogServiceImpl implements AuditLogService {

    private static final DateTimeFormatter OPERATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Resource
    private QcAuditLogMapper auditLogMapper;

    @Override
    public IPage<QcAuditLog> pageAuditLogs(QcAuditLogPageQuery query) {
        Page<QcAuditLog> page = buildPage(query);
        LambdaQueryWrapper<QcAuditLog> wrapper = buildAuditLogQueryWrapper(query);
        return auditLogMapper.selectPage(page, wrapper);
    }

    private Page<QcAuditLog> buildPage(QcAuditLogPageQuery query) {
        int pageNum = query.getPageNum() != null && query.getPageNum() > 0 ? query.getPageNum() : 1;
        int pageSize = query.getPageSize() != null && query.getPageSize() > 0 ? query.getPageSize() : 10;
        return new Page<>(pageNum, pageSize);
    }

    private LambdaQueryWrapper<QcAuditLog> buildAuditLogQueryWrapper(QcAuditLogPageQuery query) {
        LambdaQueryWrapper<QcAuditLog> wrapper = new LambdaQueryWrapper<QcAuditLog>()
                .orderByDesc(QcAuditLog::getOperateTime);
        applyOperationTypeFilter(wrapper, query.getOperationType());
        applyOperatorNoFilter(wrapper, query.getOperatorNo());
        applyOperateTimeRangeFilter(wrapper, resolveTimeStart(query), resolveTimeEnd(query));
        return wrapper;
    }

    private String resolveTimeStart(QcAuditLogPageQuery query) {
        return StringUtils.hasText(query.getTimeStart()) ? query.getTimeStart() : query.getStartTime();
    }

    private String resolveTimeEnd(QcAuditLogPageQuery query) {
        return StringUtils.hasText(query.getTimeEnd()) ? query.getTimeEnd() : query.getEndTime();
    }

    private void applyOperationTypeFilter(LambdaQueryWrapper<QcAuditLog> wrapper, String operationType) {
        if (StringUtils.hasText(operationType)) {
            wrapper.eq(QcAuditLog::getOperationType, operationType);
        }
    }

    private void applyOperatorNoFilter(LambdaQueryWrapper<QcAuditLog> wrapper, String operatorNo) {
        if (StringUtils.hasText(operatorNo)) {
            wrapper.eq(QcAuditLog::getOperatorNo, operatorNo);
        }
    }

    private void applyOperateTimeRangeFilter(
            LambdaQueryWrapper<QcAuditLog> wrapper,
            String timeStart,
            String timeEnd) {
        if (StringUtils.hasText(timeStart)) {
            wrapper.ge(QcAuditLog::getOperateTime, parseOperateTime(timeStart));
        }
        if (StringUtils.hasText(timeEnd)) {
            wrapper.le(QcAuditLog::getOperateTime, parseOperateTime(timeEnd));
        }
    }

    private LocalDateTime parseOperateTime(String timeText) {
        try {
            return LocalDateTime.parse(timeText, OPERATE_TIME_FORMAT);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("操作时间格式错误，请使用 yyyy-MM-dd HH:mm:ss：" + timeText);
        }
    }
}
