package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.PrfKpiTarget;
import com.example.query.PrfKpiTargetQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * KPI目标设置 Mapper接口
 */
@Mapper
public interface PrfKpiTargetMapper extends BaseMapper<PrfKpiTarget> {

    /**
     * 分页查询KPI目标（支持自定义条件，可在XML中扩展）
     *
     * @param page  分页对象
     * @param query 查询条件
     * @return 分页结果
     */
    IPage<PrfKpiTarget> selectPageByQuery(@Param("page") Page<PrfKpiTarget> page,
                                          @Param("query") PrfKpiTargetQuery query);
}
