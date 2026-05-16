package com.jhict.quality.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jhict.quality.entity.QcInspectionRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface QcInspectionRecordMapper extends BaseMapper<QcInspectionRecord> {

    /**
     * 统计待判定记录数（有记录但无最终判定结论）
     */
    @Select("SELECT COUNT(*) FROM qc_inspection_record r " +
            "WHERE r.status = 'NORMAL' " +
            "AND NOT EXISTS (" +
            "  SELECT 1 FROM qc_judgment_result j " +
            "  WHERE j.record_id = r.id AND j.is_final = 1" +
            ")")
    long countPendingJudgment();

    /**
     * 统计不合格记录数（最终结论为UNQUALIFIED）
     */
    @Select("SELECT COUNT(*) FROM qc_judgment_result " +
            "WHERE is_final = 1 AND judgment_type = 'UNQUALIFIED'")
    long countUnqualified();

    /**
     * 统计待复检数量（最终结论为NEED_REINSPECTION）
     */
    @Select("SELECT COUNT(*) FROM qc_judgment_result " +
            "WHERE is_final = 1 AND judgment_type = 'NEED_REINSPECTION'")
    long countPendingReinspection();

    /**
     * 统计待让步审批数量（最终结论为CAN_CONCESSION且无已审批的让步）
     */
    @Select("SELECT COUNT(*) FROM qc_judgment_result " +
            "WHERE is_final = 1 AND judgment_type = 'CAN_CONCESSION'")
    long countPendingConcessionApproval();
}
