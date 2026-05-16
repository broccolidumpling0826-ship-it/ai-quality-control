package com.example.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.PrfKpiTarget;
import com.example.mapper.PrfKpiTargetMapper;
import com.example.query.PrfKpiTargetQuery;
import com.example.service.PrfKpiTargetService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * KPI目标设置 服务实现类
 */
@Service
public class PrfKpiTargetServiceImpl extends ServiceImpl<PrfKpiTargetMapper, PrfKpiTarget>
        implements PrfKpiTargetService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public IPage<PrfKpiTarget> queryPage(PrfKpiTargetQuery query) {
        Page<PrfKpiTarget> page = new Page<>(query.getPageNum(), query.getPageSize());
        return baseMapper.selectPageByQuery(page, query);
    }

    @Override
    public PrfKpiTarget getById(String id) {
        return baseMapper.selectById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean add(PrfKpiTarget entity) {
        String now = LocalDateTime.now().format(FORMATTER);
        entity.setCreateDateTime(now);
        entity.setUpdateDateTime(now);
        if (entity.getIsEnable() == null) {
            entity.setIsEnable(1);
        }
        return save(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean update(PrfKpiTarget entity) {
        entity.setUpdateDateTime(LocalDateTime.now().format(FORMATTER));
        return updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean remove(String id) {
        return removeById(id);
    }
}
