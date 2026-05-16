package com.jhict.quality.service.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jhict.quality.dto.ConcessionConfirmCmd;
import com.jhict.quality.dto.QcConcessionAddCmd;
import com.jhict.quality.dto.QcConcessionPageQuery;
import com.jhict.quality.vo.QcConcessionVO;
import org.springframework.web.multipart.MultipartFile;

public interface ConcessionService {

    /**
     * 发起让步接收申请
     *
     * @param cmd 申请命令
     * @return 申请ID
     */
    String apply(QcConcessionAddCmd cmd);

    /**
     * 客户确认让步（附件强制上传，不可替换）
     *
     * @param id   申请ID
     * @param cmd  确认命令
     * @param file 确认附件（必须）
     */
    void confirm(String id, ConcessionConfirmCmd cmd, MultipartFile file);

    /**
     * 拒绝让步
     *
     * @param id         申请ID
     * @param rejectNote 拒绝备注
     * @param rejectFile 拒绝附件（可选）
     */
    void reject(String id, String rejectNote, MultipartFile rejectFile);

    /**
     * 审批通过让步（双签逻辑）
     *
     * @param id           申请ID
     * @param comment      审批意见
     * @param operatorRole 当前操作人角色（SALES_MANAGER 或 QUALITY_MANAGER）
     */
    void approve(String id, String comment, String operatorRole);

    /**
     * 分页查询让步申请
     *
     * @param pageNum       页码
     * @param pageSize      每页大小
     * @param confirmStatus 确认状态过滤
     * @param approvalStatus 审批状态过滤
     * @return 分页结果
     */
    IPage<QcConcessionVO> page(QcConcessionPageQuery query);

    /**
     * 查询让步申请详情
     *
     * @param id 申请ID
     * @return 视图对象
     */
    QcConcessionVO getById(String id);
}
