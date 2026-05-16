package com.jhict.performance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jhict.performance.api.entity.PrfKpiTarget;
import org.apache.ibatis.annotations.Mapper;

/**
 * KPI目标设置 Mapper
 */
@Mapper
public interface PrfKpiTargetMapper extends BaseMapper<PrfKpiTarget> {
}
