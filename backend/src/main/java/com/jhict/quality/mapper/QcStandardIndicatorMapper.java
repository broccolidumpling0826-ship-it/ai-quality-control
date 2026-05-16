package com.jhict.quality.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jhict.quality.entity.QcStandardIndicator;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface QcStandardIndicatorMapper extends BaseMapper<QcStandardIndicator> {

    /**
     * 查询标准下的所有指标配置
     */
    @Select("SELECT * FROM qc_standard_indicator WHERE standard_id = #{standardId}")
    List<QcStandardIndicator> findByStandardId(@Param("standardId") String standardId);

    /**
     * 批量删除标准下的所有指标配置
     */
    @Select("DELETE FROM qc_standard_indicator WHERE standard_id = #{standardId}")
    void deleteByStandardId(@Param("standardId") String standardId);
}
