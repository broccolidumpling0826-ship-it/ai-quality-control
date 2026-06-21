package com.jhict.quality.service.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jhict.quality.dto.QcInspectionRecordAddCmd;
import com.jhict.quality.dto.QcInspectionRecordPageQuery;
import com.jhict.quality.entity.QcInspectionRecord;
import com.jhict.quality.entity.QcJudgmentResult;
import com.jhict.quality.vo.InspectionResultVO;

import java.util.Map;

/**
 * 检验记录服务接口
 */
public interface InspectionService {

    /**
     * 新增检验记录并触发自动判定
     *
     * @param cmd 新增命令（含检验值列表）
     * @return 检验提交结果（含判定结论信息）
     */
    InspectionResultVO addRecord(QcInspectionRecordAddCmd cmd);

    /**
     * 作废检验记录
     *
     * @param id     检验记录ID
     * @param reason 作废原因
     */
    void voidRecord(String id, String reason);

    /**
     * 分页查询检验记录
     *
     * @param query 查询条件
     * @return 分页结果
     */
    IPage<QcInspectionRecord> page(QcInspectionRecordPageQuery query);

    /**
     * 查询检验记录详情（含检验值列表）
     *
     * @param id 检验记录ID
     * @return 详情Map（含record和values字段）
     */
    Map<String, Object> getById(String id);

    /**
     * 按人工裁决后的控制标准重新判定。
     *
     * @param recordId           检验记录ID
     * @param decisionStandardId 裁决控制标准ID
     * @return 新的最终判定结论
     */
    QcJudgmentResult rejudgeWithStandard(String recordId, String decisionStandardId);
}
