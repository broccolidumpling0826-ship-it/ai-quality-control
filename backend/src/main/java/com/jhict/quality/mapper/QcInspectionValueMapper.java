package com.jhict.quality.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jhict.quality.entity.QcInspectionValue;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface QcInspectionValueMapper extends BaseMapper<QcInspectionValue> {

    /**
     * 查询检验记录下的所有检验值
     */
    @Select("SELECT * FROM qc_inspection_value WHERE record_id = #{recordId}")
    List<QcInspectionValue> findByRecordId(@Param("recordId") String recordId);
}
