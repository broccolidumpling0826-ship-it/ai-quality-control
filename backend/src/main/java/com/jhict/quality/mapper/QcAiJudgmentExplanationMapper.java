package com.jhict.quality.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jhict.quality.entity.QcAiJudgmentExplanation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface QcAiJudgmentExplanationMapper extends BaseMapper<QcAiJudgmentExplanation> {

    @Select("SELECT * FROM qc_ai_judgment_explanation WHERE judgment_id = #{judgmentId} LIMIT 1")
    QcAiJudgmentExplanation findByJudgmentId(@Param("judgmentId") String judgmentId);
}
