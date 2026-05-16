package com.jhict.oa.service;

import cn.hutool.core.collection.ListUtil;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jhict.common.core.constant.BusConstant;
import com.jhict.common.core.util.BusinessUtil;
import com.jhict.oa.api.entity.OaLeaveRecord;
import com.jhict.oa.api.query.OaLeaveRecordQuery;
import com.jhict.oa.mapper.OaLeaveRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 请假记录 Service
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class OaLeaveRecordService extends JhServiceImpl<OaLeaveRecordMapper, OaLeaveRecord> {

    // TODO: 根据业务需求填充需要非空校验和唯一性校验的字段
    private static final List<SFunction<OaLeaveRecord, ?>> NOT_BLANK_FIELDS = ListUtil.of(
        // OaLeaveRecord::getEmployeeNo
    );

    private static final List<SFunction<OaLeaveRecord, ?>> UNIQUE_FIELDS = ListUtil.of(
        // OaLeaveRecord::getXxx
    );

    // ==================== 查询方法 ====================

    public OaLeaveRecord getById(String id) {
        return super.getById(id);
    }

    public List<OaLeaveRecord> list(OaLeaveRecordQuery query) {
        return lambdaQuery()
            // .eq(OaLeaveRecord::getEmployeeNo, query.getEmployeeNo())
            // .eq(OaLeaveRecord::getDeptCode, query.getDeptCode())
            // .eq(OaLeaveRecord::getLeaveType, query.getLeaveType())
            // .eq(OaLeaveRecord::getValid, query.getValid())
            .list();
    }

    public Page<OaLeaveRecord> page(OaLeaveRecordQuery query) {
        return lambdaQuery()
            // .eq(OaLeaveRecord::getEmployeeNo, query.getEmployeeNo())
            // .eq(OaLeaveRecord::getDeptCode, query.getDeptCode())
            // .eq(OaLeaveRecord::getLeaveType, query.getLeaveType())
            // .eq(OaLeaveRecord::getValid, query.getValid())
            .page(new Page<>(query.getCurrent(), query.getSize()));
    }

    // ==================== 写操作 ====================

    public void save(OaLeaveRecord entity) {
        super.save(entity);
    }

    public void updateById(OaLeaveRecord entity) {
        super.updateById(entity);
    }

    public void removeById(String id) {
        super.removeById(id);
    }

    // ==================== 批量操作 ====================

    public void saveBatch(List<OaLeaveRecord> entities) {
        this.validateSaveBatch(entities);
        super.saveBatch(entities, BusConstant.DEFAULT_DB_BATCH_SIZE);
    }

    public void updateBatch(List<OaLeaveRecord> entities) {
        this.validateUpdateBatch(entities);
        super.updateBatchById(entities, BusConstant.DEFAULT_DB_BATCH_SIZE);
    }

    public void saveOrUpdateBatch(List<OaLeaveRecord> entities) {
        BusinessUtil.saveOrUpdateBatch(
            entities,
            UNIQUE_FIELDS,
            super.baseMapper,
            this::saveBatch,
            this::updateBatch
        );
    }

    // ==================== 校验方法 ====================

    private void validateSaveBatch(List<OaLeaveRecord> entities) {
        this.validateNotBlankBatch(entities);
        this.validateUniqueBatch(entities);
    }

    private void validateUpdateBatch(List<OaLeaveRecord> entities) {
        this.validateNotBlankBatch(entities);
        this.validateUniqueBatch(entities);
    }

    private void validateNotBlankBatch(List<OaLeaveRecord> entities) {
        BusinessUtil.validateNotBlankBatch(entities, NOT_BLANK_FIELDS);
    }

    private void validateUniqueBatch(List<OaLeaveRecord> entities) {
        BusinessUtil.validateUniqueBatch(super.baseMapper, entities, UNIQUE_FIELDS);
    }

    public static String getUniqueTag(OaLeaveRecord entity) {
        return BusinessUtil.extractUniqueTag(entity, UNIQUE_FIELDS);
    }
}
