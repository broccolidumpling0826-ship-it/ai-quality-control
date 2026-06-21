package com.jhict.quality.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jhict.quality.entity.AlternativeStock;
import com.jhict.quality.mapper.AlternativeStockMapper;
import com.jhict.quality.service.api.AlternativeStockService;
import com.jhict.quality.vo.AlternativeStockVO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AlternativeStockServiceImpl implements AlternativeStockService {

    @Resource
    private AlternativeStockMapper alternativeStockMapper;

    @Override
    public List<AlternativeStockVO> findAvailable(String variety, String grade, String productSpec, int deliveryDays) {
        LocalDate latestShipDate = LocalDate.now().plusDays(Math.max(deliveryDays, 0));
        return alternativeStockMapper.selectList(new LambdaQueryWrapper<AlternativeStock>()
                        .eq(AlternativeStock::getVariety, variety)
                        .eq(AlternativeStock::getGrade, grade)
                        .eq(AlternativeStock::getStatus, "AVAILABLE")
                        .le(AlternativeStock::getEarliestShipDate, latestShipDate)
                        .orderByAsc(AlternativeStock::getEarliestShipDate))
                .stream()
                .filter(stock -> !StringUtils.hasText(productSpec)
                        || !StringUtils.hasText(stock.getSpecRange())
                        || stock.getSpecRange().contains(productSpec)
                        || productSpec.contains(stock.getSpecRange()))
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    private AlternativeStockVO toVO(AlternativeStock stock) {
        AlternativeStockVO vo = new AlternativeStockVO();
        vo.setId(stock.getId());
        vo.setVariety(stock.getVariety());
        vo.setGrade(stock.getGrade());
        vo.setSpecRange(stock.getSpecRange());
        vo.setCoilNo(stock.getCoilNo());
        vo.setBatchNo(stock.getBatchNo());
        vo.setAvailableWeight(stock.getAvailableWeight());
        vo.setLocation(stock.getLocation());
        vo.setStatus(stock.getStatus());
        vo.setEarliestShipDate(stock.getEarliestShipDate());
        vo.setRemark(stock.getRemark());
        return vo;
    }
}
