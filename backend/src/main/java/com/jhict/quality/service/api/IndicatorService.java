package com.jhict.quality.service.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jhict.quality.dto.QcIndicatorItemAddCmd;
import com.jhict.quality.dto.QcIndicatorItemPageQuery;
import com.jhict.quality.vo.QcIndicatorItemVO;

import java.util.List;

/**
 * 指标项目服务接口
 */
public interface IndicatorService {

    IPage<QcIndicatorItemVO> page(QcIndicatorItemPageQuery query);

    String add(QcIndicatorItemAddCmd cmd);

    void update(String id, QcIndicatorItemAddCmd cmd);

    void updateStatus(String id, String status);

    List<QcIndicatorItemVO> listForSelect(String keyword, String category);
}
