package com.jhict.quality.service.api;

import com.jhict.quality.vo.AlternativeStockVO;

import java.util.List;

public interface AlternativeStockService {

    List<AlternativeStockVO> findAvailable(String variety, String grade, String productSpec, int deliveryDays);
}
