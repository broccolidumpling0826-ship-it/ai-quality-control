package com.jhict.quality.service.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jhict.quality.dto.QcRejudgmentRequestAddCmd;
import com.jhict.quality.dto.RejudgmentApproveCmd;
import com.jhict.quality.vo.QcRejudgmentListVO;
import com.jhict.quality.vo.QcRejudgmentRequestVO;

public interface RejudgmentService {

    /**
     * 申请改判
     *
     * @param cmd 改判申请命令
     * @return 申请ID
     */
    String apply(QcRejudgmentRequestAddCmd cmd);

    /**
     * 审批改判申请
     *
     * @param requestId 申请ID
     * @param cmd       审批命令
     */
    void approve(String requestId, RejudgmentApproveCmd cmd);

    /**
     * 分页查询改判申请
     *
     * @param pageNum       页码
     * @param pageSize      每页大小
     * @param approvalStatus 审批状态过滤
     * @param isReverse     是否逆向改判过滤
     * @return 分页结果
     */
    IPage<QcRejudgmentListVO> page(int pageNum, int pageSize, String approvalStatus, Integer isReverse);

    /**
     * 查询改判申请详情（含审批历史）
     *
     * @param id 申请ID
     * @return 视图对象
     */
    QcRejudgmentRequestVO getById(String id);
}
