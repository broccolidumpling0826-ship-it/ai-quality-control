package com.jhict.quality.service.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jhict.quality.dto.QcAuditLogPageQuery;
import com.jhict.quality.entity.QcAuditLog;

public interface AuditLogService {

    /**
     * 分页查询审计日志
     *
     * @param query 查询条件
     * @return 分页结果
     */
    IPage<QcAuditLog> pageAuditLogs(QcAuditLogPageQuery query);
}
