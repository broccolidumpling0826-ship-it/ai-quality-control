package com.jhict.quality.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jhict.quality.entity.QcEvaluationRun;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface QcEvaluationRunMapper extends BaseMapper<QcEvaluationRun> {

    @Select("SELECT * FROM qc_evaluation_run WHERE run_no = #{runNo} LIMIT 1")
    QcEvaluationRun findByRunNo(@Param("runNo") String runNo);
}
