package com.jhict.quality.service.api;

import com.jhict.quality.dto.StandardConflictResolveCmd;
import com.jhict.quality.vo.StandardConflictVO;

/**
 * 标准冲突业务编排服务。只串联 Service，不直接访问 Mapper。
 */
public interface StandardConflictWorkflowService {

    /**
     * 完成人工裁决并按裁决标准重新判定。
     *
     * @param conflictId 冲突ID
     * @param cmd        裁决命令
     * @return 更新后的冲突详情
     */
    StandardConflictVO resolveAndRejudge(String conflictId, StandardConflictResolveCmd cmd);
}
