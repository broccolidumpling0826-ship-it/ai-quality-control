package com.jhict.quality.service.api;

import com.jhict.quality.vo.DashboardMessageVO;
import com.jhict.quality.vo.DashboardPendingItemVO;
import com.jhict.quality.vo.DashboardSummaryVO;
import com.jhict.quality.vo.DemoScenarioVO;

import java.util.List;

/**
 * 质量工作台服务
 */
public interface DashboardService {

    List<DashboardPendingItemVO> listPendingItems(String userNo);

    List<DashboardMessageVO> listMessages(String userNo);

    List<DemoScenarioVO> listDemoScenarios();

    DashboardSummaryVO getOverview();
}
