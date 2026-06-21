package com.jhict.quality.service.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jhict.quality.dto.StandardConflictPageQuery;
import com.jhict.quality.dto.StandardConflictCreateCmd;
import com.jhict.quality.dto.StandardConflictResolveCmd;
import com.jhict.quality.entity.StandardConflict;
import com.jhict.quality.vo.StandardConflictVO;

import java.util.List;

/**
 * 标准冲突服务。该服务是 standard_conflict 表的唯一 Mapper 入口。
 */
public interface StandardConflictService {

    /**
     * 保存判定过程中检测出的标准冲突。
     *
     * @param recordId   检验记录ID
     * @param judgmentId 判定ID
     * @param conflicts  冲突创建命令
     * @return 已保存冲突
     */
    List<StandardConflict> saveDetectedConflicts(String recordId, String judgmentId, List<StandardConflictCreateCmd> conflicts);

    IPage<StandardConflictVO> page(StandardConflictPageQuery query);

    StandardConflictVO getById(String id);

    StandardConflict resolveDecision(String id, StandardConflictResolveCmd cmd);

    void bindRejudgeJudgment(String id, String rejudgeJudgmentId);

    /**
     * 判断检验记录是否存在未裁决的阻断冲突。
     *
     * @param recordId 检验记录ID
     * @return true 表示存在未裁决阻断冲突
     */
    boolean hasUnresolvedBlockingConflict(String recordId);

    /**
     * 统计待人工裁决的阻断标准冲突数量。
     *
     * @return 待裁决阻断冲突数
     */
    Long countPendingBlockingConflicts();

    /**
     * 按时间范围统计标准冲突数量。
     *
     * @param timeStart 开始时间，可为空
     * @param timeEnd   结束时间，可为空
     * @return 标准冲突数量
     */
    Long countConflicts(String timeStart, String timeEnd);

    /**
     * 按时间范围统计已裁决标准冲突数量。
     *
     * @param timeStart 开始时间，可为空
     * @param timeEnd   结束时间，可为空
     * @return 已裁决冲突数量
     */
    Long countResolvedConflicts(String timeStart, String timeEnd);
}
