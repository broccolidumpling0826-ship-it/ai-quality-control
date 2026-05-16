package com.jhict.performance.service;

import cn.hutool.core.collection.ListUtil;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jhict.common.core.constant.BusConstant;
import com.jhict.common.core.util.BusinessUtil;
import com.jhict.performance.api.entity.PrfKpiTarget;
import com.jhict.performance.api.query.PrfKpiTargetQuery;
import com.jhict.performance.mapper.PrfKpiTargetMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * KPI目标设置 Service
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class PrfKpiTargetService extends JhServiceImpl<PrfKpiTargetMapper, PrfKpiTarget> {

    // TODO: 根据业务需求填充需要非空校验和唯一性校验的字段
    private static final List<SFunction<PrfKpiTarget, ?>> NOT_BLANK_FIELDS = ListUtil.of(
        // PrfKpiTarget::getKpiCode,
        // PrfKpiTarget::getKpiName,
        // PrfKpiTarget::getYearNo
    );

    private static final List<SFunction<PrfKpiTarget, ?>> UNIQUE_FIELDS = ListUtil.of(
        // PrfKpiTarget::getKpiCode
    );

    // ==================== 查询方法 ====================

    public PrfKpiTarget getById(String id) {
        return super.getById(id);
    }

    public List<PrfKpiTarget> list(PrfKpiTargetQuery query) {
        return lambdaQuery()
            // .eq(PrfKpiTarget::getKpiCode, query.getKpiCode())  // 按需添加查询条件
            // .like(PrfKpiTarget::getKpiName, query.getKpiName())
            // .eq(PrfKpiTarget::getYearNo, query.getYearNo())
            // .eq(PrfKpiTarget::getMonthNo, query.getMonthNo())
            .list();
    }

    public Page<PrfKpiTarget> page(PrfKpiTargetQuery query) {
        return lambdaQuery()
            // .eq(PrfKpiTarget::getKpiCode, query.getKpiCode())  // 按需添加查询条件
            // .like(PrfKpiTarget::getKpiName, query.getKpiName())
            // .eq(PrfKpiTarget::getYearNo, query.getYearNo())
            // .eq(PrfKpiTarget::getMonthNo, query.getMonthNo())
            .page(new Page<>(query.getCurrent(), query.getSize()));
    }

    // ==================== 写操作 ====================

    public void save(PrfKpiTarget entity) {
        super.save(entity);
    }

    public void updateById(PrfKpiTarget entity) {
        super.updateById(entity);
    }

    public void removeById(String id) {
        super.removeById(id);
    }

    // ==================== 批量操作 ====================

    public void saveBatch(List<PrfKpiTarget> entities) {
        this.validateSaveBatch(entities);
        super.saveBatch(entities, BusConstant.DEFAULT_DB_BATCH_SIZE);
    }

    public void updateBatch(List<PrfKpiTarget> entities) {
        this.validateUpdateBatch(entities);
        super.updateBatchById(entities, BusConstant.DEFAULT_DB_BATCH_SIZE);
    }

    public void saveOrUpdateBatch(List<PrfKpiTarget> entities) {
        BusinessUtil.saveOrUpdateBatch(
            entities,
            UNIQUE_FIELDS,
            super.baseMapper,
            this::saveBatch,
            this::updateBatch
        );
    }

    // ==================== 校验方法 ====================

    private void validateSaveBatch(List<PrfKpiTarget> entities) {
        this.validateNotBlankBatch(entities);
        this.validateUniqueBatch(entities);
    }

    private void validateUpdateBatch(List<PrfKpiTarget> entities) {
        this.validateNotBlankBatch(entities);
        this.validateUniqueBatch(entities);
    }

    private void validateNotBlankBatch(List<PrfKpiTarget> entities) {
        BusinessUtil.validateNotBlankBatch(entities, NOT_BLANK_FIELDS);
    }

    private void validateUniqueBatch(List<PrfKpiTarget> entities) {
        BusinessUtil.validateUniqueBatch(super.baseMapper, entities, UNIQUE_FIELDS);
    }

    public static String getUniqueTag(PrfKpiTarget entity) {
        return BusinessUtil.extractUniqueTag(entity, UNIQUE_FIELDS);
    }
}
