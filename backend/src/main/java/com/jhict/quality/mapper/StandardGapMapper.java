package com.jhict.quality.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jhict.quality.entity.StandardGap;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface StandardGapMapper extends BaseMapper<StandardGap> {

    /**
     * 查询是否已存在未解决的缺口记录（按 variety+grade+indicatorId）
     */
    @Select("SELECT * FROM standard_gap " +
            "WHERE variety = #{variety} " +
            "AND grade = #{grade} " +
            "AND indicator_id = #{indicatorId} " +
            "AND is_resolved = 0 " +
            "LIMIT 1")
    StandardGap findUnresolvedGap(
            @Param("variety") String variety,
            @Param("grade") String grade,
            @Param("indicatorId") String indicatorId
    );
}
