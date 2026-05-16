package com.jhict.quality.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jhict.quality.entity.QcJudgmentResult;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface QcJudgmentResultMapper extends BaseMapper<QcJudgmentResult> {

    /**
     * 查询检验记录的最终判定结论
     */
    @Select("SELECT * FROM qc_judgment_result WHERE record_id = #{recordId} AND is_final = 1 LIMIT 1")
    QcJudgmentResult findFinalByRecordId(@Param("recordId") String recordId);

    /**
     * 将检验记录的所有最终判定结论置为非最终（改判时使用）
     */
    @Update("UPDATE qc_judgment_result SET is_final = 0 WHERE record_id = #{recordId} AND is_final = 1")
    void clearFinalByRecordId(@Param("recordId") String recordId);

    /**
     * 将指定记录的最终判定结论置为非最终（作废时使用）
     */
    @Update("UPDATE qc_judgment_result SET is_final = 0 WHERE record_id = #{recordId}")
    void invalidateByRecordId(@Param("recordId") String recordId);
}
