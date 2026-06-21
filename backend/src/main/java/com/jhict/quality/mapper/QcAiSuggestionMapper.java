package com.jhict.quality.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jhict.quality.entity.QcAiSuggestion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface QcAiSuggestionMapper extends BaseMapper<QcAiSuggestion> {

    @Select("SELECT * FROM qc_ai_suggestion WHERE suggestion_type = #{type} AND ref_id = #{refId} "
            + "ORDER BY create_date_time DESC LIMIT 1")
    QcAiSuggestion findLatest(@Param("type") String type, @Param("refId") String refId);
}
