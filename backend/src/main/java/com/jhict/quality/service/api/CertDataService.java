package com.jhict.quality.service.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jhict.quality.dto.QcQualityCertGenerateCmd;
import com.jhict.quality.vo.QcQualityCertDataVO;

public interface CertDataService {

    /**
     * 生成质保书数据
     *
     * @param cmd 生成命令
     * @return 质保书数据详情（含指标快照）
     */
    QcQualityCertDataVO generate(QcQualityCertGenerateCmd cmd);

    /**
     * 按 ID 查询质保书数据详情
     */
    QcQualityCertDataVO getById(String id);

    /**
     * 分页查询质保书数据
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @param coilNo   卷号过滤
     * @param batchNo  批次号过滤
     * @return 分页结果
     */
    IPage<QcQualityCertDataVO> page(int pageNum, int pageSize, String coilNo, String batchNo);
}
