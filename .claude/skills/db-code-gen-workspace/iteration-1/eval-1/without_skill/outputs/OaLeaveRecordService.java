package com.example.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.entity.OaLeaveRecord;
import com.example.query.OaLeaveRecordQuery;

/**
 * 请假记录 Service 接口
 */
public interface OaLeaveRecordService {

    /**
     * 分页查询请假记录
     *
     * @param query 查询条件
     * @return 分页结果
     */
    IPage<OaLeaveRecord> page(OaLeaveRecordQuery query);

    /**
     * 根据主键查询请假记录
     *
     * @param id 主键
     * @return 请假记录
     */
    OaLeaveRecord getById(String id);

    /**
     * 新增请假记录
     *
     * @param record 请假记录
     */
    void save(OaLeaveRecord record);

    /**
     * 修改请假记录
     *
     * @param record 请假记录
     */
    void update(OaLeaveRecord record);

    /**
     * 根据主键删除请假记录（逻辑删除，设置 IS_VALID=0）
     *
     * @param id 主键
     */
    void deleteById(String id);
}
