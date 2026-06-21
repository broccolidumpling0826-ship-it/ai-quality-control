package com.jhict.quality.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jhict.quality.entity.QcAiPromptVersion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface QcAiPromptVersionMapper extends BaseMapper<QcAiPromptVersion> {

    @Select("SELECT * FROM qc_ai_prompt_version WHERE prompt_key = #{promptKey} AND is_active = 1 LIMIT 1")
    QcAiPromptVersion findActiveByPromptKey(@Param("promptKey") String promptKey);
}
