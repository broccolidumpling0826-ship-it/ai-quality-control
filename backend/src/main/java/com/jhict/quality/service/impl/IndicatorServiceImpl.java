package com.jhict.quality.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jhict.quality.common.entity.ApiResult;
import com.jhict.quality.common.exception.ServiceException;
import com.jhict.quality.dto.QcIndicatorItemAddCmd;
import com.jhict.quality.dto.QcIndicatorItemPageQuery;
import com.jhict.quality.entity.QcIndicatorItem;
import com.jhict.quality.mapper.QcIndicatorItemMapper;
import com.jhict.quality.service.api.IndicatorService;
import com.jhict.quality.vo.QcIndicatorItemVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class IndicatorServiceImpl implements IndicatorService {

    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String STATUS_INACTIVE = "INACTIVE";

    @Resource
    private QcIndicatorItemMapper indicatorItemMapper;

    @Override
    public IPage<QcIndicatorItemVO> page(QcIndicatorItemPageQuery query) {
        int pageNum = query.getPageNum() != null && query.getPageNum() > 0 ? query.getPageNum() : 1;
        int pageSize = query.getPageSize() != null && query.getPageSize() > 0 ? query.getPageSize() : 20;

        LambdaQueryWrapper<QcIndicatorItem> wrapper = buildListWrapper(query.getIndicatorName(), query.getCategory());
        Page<QcIndicatorItem> page = new Page<>(pageNum, pageSize);
        IPage<QcIndicatorItem> result = indicatorItemMapper.selectPage(page, wrapper);

        Page<QcIndicatorItemVO> voPage = new Page<>(pageNum, pageSize, result.getTotal());
        voPage.setRecords(result.getRecords().stream().map(this::toVo).collect(Collectors.toList()));
        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String add(QcIndicatorItemAddCmd cmd) {
        assertCodeUnique(cmd.getIndicatorCode(), null);

        QcIndicatorItem item = fromCmd(cmd);
        item.setStatus(STATUS_ACTIVE);
        indicatorItemMapper.insert(item);
        log.info("新增指标项目，id={}, code={}", item.getId(), item.getIndicatorCode());
        return item.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(String id, QcIndicatorItemAddCmd cmd) {
        QcIndicatorItem existing = requireExisting(id);
        assertCodeUnique(cmd.getIndicatorCode(), id);

        QcIndicatorItem item = fromCmd(cmd);
        item.setId(existing.getId());
        item.setStatus(existing.getStatus());
        indicatorItemMapper.updateById(item);
        log.info("更新指标项目，id={}, code={}", id, cmd.getIndicatorCode());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(String id, String status) {
        if (!STATUS_ACTIVE.equals(status) && !STATUS_INACTIVE.equals(status)) {
            throw new ServiceException(ApiResult.CODE_BAD_REQUEST, "状态值无效，仅支持 ACTIVE / INACTIVE");
        }
        QcIndicatorItem existing = requireExisting(id);
        existing.setStatus(status);
        indicatorItemMapper.updateById(existing);
        log.info("更新指标状态，id={}, status={}", id, status);
    }

    @Override
    public List<QcIndicatorItemVO> listForSelect(String keyword, String category) {
        LambdaQueryWrapper<QcIndicatorItem> wrapper = buildListWrapper(keyword, category)
                .eq(QcIndicatorItem::getStatus, STATUS_ACTIVE);
        return indicatorItemMapper.selectList(wrapper).stream()
                .map(this::toVo)
                .collect(Collectors.toList());
    }

    private LambdaQueryWrapper<QcIndicatorItem> buildListWrapper(String keyword, String category) {
        return new LambdaQueryWrapper<QcIndicatorItem>()
                .and(StringUtils.hasText(keyword), w -> w
                        .like(QcIndicatorItem::getIndicatorName, keyword)
                        .or()
                        .like(QcIndicatorItem::getIndicatorCode, keyword))
                .eq(StringUtils.hasText(category), QcIndicatorItem::getIndicatorCategory, category)
                .orderByAsc(QcIndicatorItem::getIndicatorCode);
    }

    private QcIndicatorItem requireExisting(String id) {
        QcIndicatorItem existing = indicatorItemMapper.selectById(id);
        if (existing == null) {
            throw new ServiceException(ApiResult.CODE_NOT_FOUND, "指标项目不存在");
        }
        return existing;
    }

    private void assertCodeUnique(String indicatorCode, String excludeId) {
        LambdaQueryWrapper<QcIndicatorItem> wrapper = new LambdaQueryWrapper<QcIndicatorItem>()
                .eq(QcIndicatorItem::getIndicatorCode, indicatorCode);
        if (StringUtils.hasText(excludeId)) {
            wrapper.ne(QcIndicatorItem::getId, excludeId);
        }
        if (indicatorItemMapper.selectCount(wrapper) > 0) {
            throw new ServiceException(ApiResult.CODE_BAD_REQUEST, "指标代码已存在：" + indicatorCode);
        }
    }

    private QcIndicatorItem fromCmd(QcIndicatorItemAddCmd cmd) {
        QcIndicatorItem item = new QcIndicatorItem();
        item.setIndicatorName(cmd.getIndicatorName());
        item.setIndicatorCode(cmd.getIndicatorCode());
        item.setIndicatorCategory(cmd.getCategory());
        item.setUnit(cmd.getUnit());
        item.setTestMethod(cmd.getTestMethod());
        item.setDescription(cmd.getRemark());
        return item;
    }

    private QcIndicatorItemVO toVo(QcIndicatorItem item) {
        QcIndicatorItemVO vo = new QcIndicatorItemVO();
        vo.setId(item.getId());
        vo.setIndicatorName(item.getIndicatorName());
        vo.setIndicatorCode(item.getIndicatorCode());
        vo.setIndicatorCategory(item.getIndicatorCategory());
        vo.setCategory(item.getIndicatorCategory());
        vo.setUnit(item.getUnit());
        vo.setTestMethod(item.getTestMethod());
        vo.setDescription(item.getDescription());
        vo.setRemark(item.getDescription());
        vo.setStatus(StringUtils.hasText(item.getStatus()) ? item.getStatus() : STATUS_ACTIVE);
        return vo;
    }
}
