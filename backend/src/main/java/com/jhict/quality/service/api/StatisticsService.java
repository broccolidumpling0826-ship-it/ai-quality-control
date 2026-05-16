package com.jhict.quality.service.api;

import com.jhict.quality.vo.IndicatorDistributionVO;
import com.jhict.quality.vo.StatisticsOverviewVO;

import java.util.List;

public interface StatisticsService {

    /**
     * 获取质量统计概览
     *
     * @param timeStart 开始时间（yyyy-MM-dd HH:mm:ss，可为空）
     * @param timeEnd   结束时间（yyyy-MM-dd HH:mm:ss，可为空）
     * @return 统计概览
     */
    StatisticsOverviewVO getOverview(String timeStart, String timeEnd);

    /**
     * 获取指标不合格分布统计
     *
     * @param timeStart 开始时间（可为空）
     * @param timeEnd   结束时间（可为空）
     * @return 分布列表
     */
    List<IndicatorDistributionVO> getIndicatorDistribution(String timeStart, String timeEnd);
}
