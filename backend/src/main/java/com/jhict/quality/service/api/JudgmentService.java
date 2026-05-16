package com.jhict.quality.service.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jhict.quality.dto.QcJudgmentPageQuery;
import com.jhict.quality.engine.model.JudgmentOutput;
import com.jhict.quality.entity.QcJudgmentResult;
import com.jhict.quality.vo.DashboardSummaryVO;
import com.jhict.quality.vo.QcJudgmentListVO;
import com.jhict.quality.vo.QcJudgmentResultVO;

/**
 * 判定结论服务接口
 */
public interface JudgmentService {

    /**
     * 保存判定结论（含Evidence快照和StandardGap）
     *
     * @param recordId 检验记录ID
     * @param output   判定引擎输出
     * @return 保存后的判定结论实体
     */
    QcJudgmentResult saveJudgmentResult(String recordId, JudgmentOutput output);

    /**
     * 查询检验记录的当前最终判定结论
     *
     * @param recordId 检验记录ID
     * @return 判定结论VO
     */
    QcJudgmentResultVO getCurrentJudgment(String recordId);

    /**
     * 查询判定结论详情（含完整evidence和标准优先级匹配过程）
     *
     * @param id 判定结论ID
     * @return 判定结论详情VO
     */
    QcJudgmentResultVO getExplanation(String id);

    /**
     * 分页查询判定结论
     *
     * @param query 查询条件
     * @return 分页结果
     */
    IPage<QcJudgmentListVO> page(QcJudgmentPageQuery query);

    /**
     * 获取看板汇总数据（带Redis缓存，TTL=60s）
     *
     * @return 看板汇总VO
     */
    DashboardSummaryVO getDashboardSummary();
}
