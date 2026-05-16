package com.jhict.quality.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jhict.quality.entity.QcJudgmentEvidence;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface QcJudgmentEvidenceMapper extends BaseMapper<QcJudgmentEvidence> {

    /**
     * 查询判定结论下的所有依据
     */
    @Select("SELECT * FROM qc_judgment_evidence WHERE judgment_id = #{judgmentId}")
    List<QcJudgmentEvidence> findByJudgmentId(@Param("judgmentId") String judgmentId);
}
