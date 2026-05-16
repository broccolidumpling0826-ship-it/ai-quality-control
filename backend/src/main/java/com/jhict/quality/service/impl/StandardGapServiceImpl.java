package com.jhict.quality.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jhict.quality.common.entity.ApiResult;
import com.jhict.quality.common.exception.ServiceException;
import com.jhict.quality.dto.StandardGapPageQuery;
import com.jhict.quality.entity.QcIndicatorItem;
import com.jhict.quality.entity.StandardGap;
import com.jhict.quality.mapper.QcIndicatorItemMapper;
import com.jhict.quality.mapper.StandardGapMapper;
import com.jhict.quality.service.api.StandardGapService;
import com.jhict.quality.vo.StandardGapVO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StandardGapServiceImpl implements StandardGapService {

    private static final int RESOLVED = 1;

    @Resource
    private StandardGapMapper standardGapMapper;

    @Resource
    private QcIndicatorItemMapper indicatorItemMapper;

    @Override
    public IPage<StandardGapVO> pageStandardGaps(StandardGapPageQuery query) {
        Page<StandardGap> page = buildPage(query);
        LambdaQueryWrapper<StandardGap> wrapper = buildGapQueryWrapper(query);
        IPage<StandardGap> gapPage = standardGapMapper.selectPage(page, wrapper);
        Map<String, String> indicatorNameMap = loadIndicatorNameMap(gapPage.getRecords());
        return gapPage.convert(gap -> toGapVo(gap, indicatorNameMap));
    }

    @Override
    public void resolveStandardGap(String id) {
        StandardGap gap = requireExistingGap(id);
        gap.setIsResolved(RESOLVED);
        standardGapMapper.updateById(gap);
    }

    private Page<StandardGap> buildPage(StandardGapPageQuery query) {
        int pageNum = query.getPageNum() != null && query.getPageNum() > 0 ? query.getPageNum() : 1;
        int pageSize = query.getPageSize() != null && query.getPageSize() > 0 ? query.getPageSize() : 20;
        return new Page<>(pageNum, pageSize);
    }

    private LambdaQueryWrapper<StandardGap> buildGapQueryWrapper(StandardGapPageQuery query) {
        LambdaQueryWrapper<StandardGap> wrapper = new LambdaQueryWrapper<StandardGap>()
                .orderByDesc(StandardGap::getFirstFoundTime);
        applyVarietyFilter(wrapper, query.getVariety());
        applyGradeFilter(wrapper, query.getGrade());
        applyResolvedFilter(wrapper, query.getIsResolved());
        return wrapper;
    }

    private void applyVarietyFilter(LambdaQueryWrapper<StandardGap> wrapper, String variety) {
        if (StringUtils.hasText(variety)) {
            wrapper.like(StandardGap::getVariety, variety);
        }
    }

    private void applyGradeFilter(LambdaQueryWrapper<StandardGap> wrapper, String grade) {
        if (StringUtils.hasText(grade)) {
            wrapper.like(StandardGap::getGrade, grade);
        }
    }

    private void applyResolvedFilter(LambdaQueryWrapper<StandardGap> wrapper, Integer isResolved) {
        if (isResolved != null) {
            wrapper.eq(StandardGap::getIsResolved, isResolved);
        }
    }

    private Map<String, String> loadIndicatorNameMap(List<StandardGap> gaps) {
        List<String> indicatorIds = gaps.stream()
                .map(StandardGap::getIndicatorId)
                .distinct()
                .collect(Collectors.toList());
        if (indicatorIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<QcIndicatorItem> items = indicatorItemMapper.selectList(
                new LambdaQueryWrapper<QcIndicatorItem>().in(QcIndicatorItem::getId, indicatorIds));
        return items.stream()
                .collect(Collectors.toMap(QcIndicatorItem::getId, QcIndicatorItem::getIndicatorName, (a, b) -> a));
    }

    private StandardGapVO toGapVo(StandardGap gap, Map<String, String> indicatorNameMap) {
        StandardGapVO vo = new StandardGapVO();
        vo.setId(gap.getId());
        vo.setVariety(gap.getVariety());
        vo.setGrade(gap.getGrade());
        vo.setIndicatorId(gap.getIndicatorId());
        vo.setIndicatorName(indicatorNameMap.getOrDefault(gap.getIndicatorId(), gap.getIndicatorId()));
        vo.setFirstFoundTime(gap.getFirstFoundTime());
        vo.setRelatedRecordId(gap.getRelatedRecordId());
        vo.setIsResolved(gap.getIsResolved());
        return vo;
    }

    private StandardGap requireExistingGap(String id) {
        StandardGap gap = standardGapMapper.selectById(id);
        if (gap == null) {
            throw new ServiceException(ApiResult.CODE_NOT_FOUND, "缺口记录不存在，id=" + id);
        }
        return gap;
    }
}
