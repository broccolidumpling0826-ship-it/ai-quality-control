package com.jhict.quality.service.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jhict.quality.dto.AiAssessmentCreateCmd;
import com.jhict.quality.dto.AiAssessmentHandleCmd;
import com.jhict.quality.dto.AiAssessmentPageQuery;
import com.jhict.quality.entity.QcAiAssessment;
import com.jhict.quality.vo.AiAssessmentVO;

/**
 * AI评估审计服务。该服务是 qc_ai_assessment 表的唯一 Mapper 入口。
 */
public interface AiAssessmentService {

    /**
     * 创建不可变 AI 评估审计记录。
     *
     * @param cmd 创建命令
     * @return 已创建 AI 评估审计记录
     */
    QcAiAssessment create(AiAssessmentCreateCmd cmd);

    /**
     * 分页查询 AI 评估审计记录。
     *
     * @param query 查询条件
     * @return AI 评估审计分页
     */
    IPage<AiAssessmentVO> page(AiAssessmentPageQuery query);

    /**
     * 查询 AI 评估审计详情。
     *
     * @param id AI评估ID
     * @return AI 评估审计详情
     */
    AiAssessmentVO getById(String id);

    /**
     * 记录人工采纳或忽略意见。
     *
     * @param id  AI评估ID
     * @param cmd 人工处理命令
     * @return 已更新处理状态的 AI 评估记录
     */
    QcAiAssessment handle(String id, AiAssessmentHandleCmd cmd);

    /**
     * 统计待人工复核的高风险 AI 建议数量。
     *
     * @return 待处理高风险 AI 建议数
     */
    Long countPendingRiskWarnings();

    /**
     * 统计待人工复核的低置信 AI 输出数量。
     *
     * @return 待处理低置信 AI 输出数
     */
    Long countPendingLowConfidenceReviews();

    /**
     * 按时间范围统计 AI 评估输出总数。
     *
     * @param timeStart 开始时间，可为空
     * @param timeEnd   结束时间，可为空
     * @return AI评估总数
     */
    Long countAssessments(String timeStart, String timeEnd);

    /**
     * 按时间范围统计已采纳 AI 评估数量。
     *
     * @param timeStart 开始时间，可为空
     * @param timeEnd   结束时间，可为空
     * @return 已采纳数量
     */
    Long countAdoptedAssessments(String timeStart, String timeEnd);

    /**
     * 按时间范围统计已人工处理 AI 评估数量。
     *
     * @param timeStart 开始时间，可为空
     * @param timeEnd   结束时间，可为空
     * @return 已处理数量
     */
    Long countHandledAssessments(String timeStart, String timeEnd);

    /**
     * 按时间范围统计低置信 AI 输出数量。
     *
     * @param timeStart 开始时间，可为空
     * @param timeEnd   结束时间，可为空
     * @return 低置信数量
     */
    Long countLowConfidenceAssessments(String timeStart, String timeEnd);
}
