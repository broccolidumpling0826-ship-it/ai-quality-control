package com.jhict.quality.service.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jhict.quality.dto.QcReinspectionAddCmd;
import com.jhict.quality.dto.ReinspectionCompleteCmd;
import com.jhict.quality.entity.QcReinspectionRecord;

public interface ReinspectionService {

    /**
     * 发起复检申请
     *
     * @param cmd 复检申请命令
     * @return 复检记录ID
     */
    String initiate(QcReinspectionAddCmd cmd);

    /**
     * 完成复检（关联新检验记录）
     *
     * @param reinspectionId 复检记录ID
     * @param cmd            完成命令
     */
    void complete(String reinspectionId, ReinspectionCompleteCmd cmd);

    /**
     * 分页查询复检记录
     *
     * @param pageNum   页码
     * @param pageSize  每页大小
     * @param status    状态过滤
     * @return 分页结果
     */
    IPage<QcReinspectionRecord> page(int pageNum, int pageSize, String status);
}
