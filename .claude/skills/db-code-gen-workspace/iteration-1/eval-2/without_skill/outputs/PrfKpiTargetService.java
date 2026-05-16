package com.example.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.PrfKpiTarget;
import com.example.query.PrfKpiTargetQuery;

/**
 * KPI目标设置 服务接口
 */
public interface PrfKpiTargetService extends IService<PrfKpiTarget> {

    /**
     * 分页查询KPI目标列表
     *
     * @param query 查询条件
     * @return 分页结果
     */
    IPage<PrfKpiTarget> queryPage(PrfKpiTargetQuery query);

    /**
     * 根据ID查询KPI目标
     *
     * @param id 主键ID
     * @return KPI目标实体
     */
    PrfKpiTarget getById(String id);

    /**
     * 新增KPI目标
     *
     * @param entity KPI目标实体
     * @return 是否成功
     */
    boolean add(PrfKpiTarget entity);

    /**
     * 修改KPI目标
     *
     * @param entity KPI目标实体
     * @return 是否成功
     */
    boolean update(PrfKpiTarget entity);

    /**
     * 删除KPI目标
     *
     * @param id 主键ID
     * @return 是否成功
     */
    boolean remove(String id);
}
