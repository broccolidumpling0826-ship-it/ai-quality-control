package com.jhict.quality.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jhict.quality.entity.QcStandardConflict;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface QcStandardConflictMapper extends BaseMapper<QcStandardConflict> {

    @Select("SELECT COUNT(*) FROM qc_standard_conflict WHERE conflict_status = 'PENDING'")
    long countPendingConflicts();

    @Select("SELECT COUNT(*) FROM qc_concession_assessment WHERE risk_level = 'HIGH'")
    long countHighRiskConcessions();

    @Select("SELECT COUNT(*) FROM qc_ai_judgment_explanation "
            + "WHERE confidence_level = 'LOW' AND manual_review_required = 1")
    long countLowConfidenceReviews();

    @Select("SELECT * FROM qc_standard_conflict "
            + "WHERE variety = #{variety} AND grade = #{grade} AND indicator_id = #{indicatorId} "
            + "AND standard_id_a = #{standardIdA} AND standard_id_b = #{standardIdB} LIMIT 1")
    QcStandardConflict findExisting(@Param("variety") String variety,
                                    @Param("grade") String grade,
                                    @Param("indicatorId") String indicatorId,
                                    @Param("standardIdA") String standardIdA,
                                    @Param("standardIdB") String standardIdB);
}
