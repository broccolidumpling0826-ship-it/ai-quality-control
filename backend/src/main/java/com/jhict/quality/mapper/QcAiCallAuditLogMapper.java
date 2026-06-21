package com.jhict.quality.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jhict.quality.entity.QcAiCallAuditLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface QcAiCallAuditLogMapper extends BaseMapper<QcAiCallAuditLog> {

    @Select("SELECT COUNT(*) FROM qc_ai_call_audit_log")
    long countAll();

    @Select("SELECT COALESCE(SUM(total_tokens), 0) FROM qc_ai_call_audit_log")
    long sumTotalTokens();

    @Select("SELECT COALESCE(AVG(latency_ms), 0) FROM qc_ai_call_audit_log WHERE latency_ms IS NOT NULL")
    double avgLatencyMs();

    @Select("SELECT COUNT(*) FROM qc_ai_call_audit_log WHERE success = 0")
    long countFailures();

    @Select("SELECT call_source AS source, COUNT(*) AS cnt FROM qc_ai_call_audit_log GROUP BY call_source")
    List<Map<String, Object>> countBySource();
}
