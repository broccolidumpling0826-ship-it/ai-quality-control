package com.jhict.quality.service.api;

import com.jhict.quality.vo.WorkflowAdviceVO;

/**
 * AI流程建议服务。仅生成复检/改判建议，不自动创建业务流程记录。
 */
public interface WorkflowAdviceService {

    /**
     * 生成复检建议。
     *
     * @param judgmentId 判定ID
     * @return 复检建议
     */
    WorkflowAdviceVO adviseReinspection(String judgmentId);

    /**
     * 生成改判建议。
     *
     * @param judgmentId 判定ID
     * @return 改判建议
     */
    WorkflowAdviceVO adviseRejudgment(String judgmentId);
}
