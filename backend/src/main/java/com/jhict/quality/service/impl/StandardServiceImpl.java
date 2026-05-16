package com.jhict.quality.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jhict.quality.common.annotation.AuditLog;
import com.jhict.quality.common.entity.ApiResult;
import com.jhict.quality.common.exception.ServiceException;
import com.jhict.quality.dto.QcQualityStandardAddCmd;
import com.jhict.quality.dto.QcQualityStandardPageQuery;
import com.jhict.quality.entity.QcIndicatorItem;
import com.jhict.quality.entity.QcQualityStandard;
import com.jhict.quality.entity.QcStandardIndicator;
import com.jhict.quality.mapper.QcIndicatorItemMapper;
import com.jhict.quality.mapper.QcQualityStandardMapper;
import com.jhict.quality.mapper.QcStandardIndicatorMapper;
import com.jhict.quality.service.api.IndicatorService;
import com.jhict.quality.service.api.StandardService;
import com.jhict.quality.vo.QcIndicatorItemVO;
import com.jhict.quality.vo.QcQualityStandardDetailVO;
import com.jhict.quality.vo.QcQualityStandardVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class StandardServiceImpl implements StandardService {

    @Resource
    private QcQualityStandardMapper qualityStandardMapper;

    @Resource
    private QcStandardIndicatorMapper standardIndicatorMapper;

    @Resource
    private QcIndicatorItemMapper indicatorItemMapper;

    @Resource
    private IndicatorService indicatorService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String addStandard(QcQualityStandardAddCmd cmd) {
        // 校验 effectiveDate < expiryDate
        if (!cmd.getEffectiveDate().isBefore(cmd.getExpiryDate())) {
            throw new ServiceException(ApiResult.CODE_BAD_REQUEST, "生效日期必须早于失效日期");
        }

        // 客户协议标准校验 customerId 必填
        if ("CUSTOMER".equals(cmd.getStandardType()) && !StringUtils.hasText(cmd.getCustomerId())) {
            throw new ServiceException(ApiResult.CODE_BAD_REQUEST, "客户协议标准必须填写客户ID");
        }

        // 构建并保存标准
        QcQualityStandard standard = new QcQualityStandard();
        standard.setStandardType(cmd.getStandardType());
        standard.setVariety(cmd.getVariety());
        standard.setGrade(cmd.getGrade());
        standard.setSpecRange(cmd.getSpecRange());
        standard.setVersionNo(cmd.getVersionNo());
        standard.setEffectiveDate(cmd.getEffectiveDate());
        standard.setExpiryDate(cmd.getExpiryDate());
        standard.setStatus("DRAFT");
        standard.setCustomerId(cmd.getCustomerId());
        standard.setRemark(cmd.getRemark());
        qualityStandardMapper.insert(standard);

        // 批量保存指标配置
        saveStandardIndicators(standard.getId(), cmd.getIndicators());

        log.info("新增质量标准，id={}, type={}, variety={}, grade={}",
                standard.getId(), cmd.getStandardType(), cmd.getVariety(), cmd.getGrade());
        return standard.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStandard(QcQualityStandardAddCmd cmd) {
        if (!StringUtils.hasText(cmd.getId())) {
            throw new ServiceException(ApiResult.CODE_BAD_REQUEST, "更新时标准ID不能为空");
        }

        QcQualityStandard existing = qualityStandardMapper.selectById(cmd.getId());
        if (existing == null) {
            throw new ServiceException(ApiResult.CODE_NOT_FOUND, "质量标准不存在");
        }
        if (!"DRAFT".equals(existing.getStatus())) {
            throw new ServiceException(ApiResult.CODE_BAD_REQUEST, "仅草稿状态的标准可以修改");
        }

        // 校验日期
        if (!cmd.getEffectiveDate().isBefore(cmd.getExpiryDate())) {
            throw new ServiceException(ApiResult.CODE_BAD_REQUEST, "生效日期必须早于失效日期");
        }

        // 客户协议标准校验
        if ("CUSTOMER".equals(cmd.getStandardType()) && !StringUtils.hasText(cmd.getCustomerId())) {
            throw new ServiceException(ApiResult.CODE_BAD_REQUEST, "客户协议标准必须填写客户ID");
        }

        // 更新标准主体
        existing.setStandardType(cmd.getStandardType());
        existing.setVariety(cmd.getVariety());
        existing.setGrade(cmd.getGrade());
        existing.setSpecRange(cmd.getSpecRange());
        existing.setVersionNo(cmd.getVersionNo());
        existing.setEffectiveDate(cmd.getEffectiveDate());
        existing.setExpiryDate(cmd.getExpiryDate());
        existing.setCustomerId(cmd.getCustomerId());
        existing.setRemark(cmd.getRemark());
        qualityStandardMapper.updateById(existing);

        // 删除旧指标配置，重新保存
        standardIndicatorMapper.delete(
                new LambdaQueryWrapper<QcStandardIndicator>()
                        .eq(QcStandardIndicator::getStandardId, cmd.getId())
        );
        saveStandardIndicators(cmd.getId(), cmd.getIndicators());

        log.info("更新质量标准，id={}", cmd.getId());
    }

    @Override
    @AuditLog(operationType = "PUBLISH_STANDARD", targetEntity = "QcQualityStandard")
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> publishStandard(String id) {
        QcQualityStandard standard = qualityStandardMapper.selectById(id);
        if (standard == null) {
            throw new ServiceException(ApiResult.CODE_NOT_FOUND, "质量标准不存在");
        }
        if (!"DRAFT".equals(standard.getStatus())) {
            throw new ServiceException(ApiResult.CODE_BAD_REQUEST, "仅草稿状态的标准可以发布");
        }

        // 校验标准下必须有至少一个指标
        long indicatorCount = standardIndicatorMapper.selectCount(
                new LambdaQueryWrapper<QcStandardIndicator>()
                        .eq(QcStandardIndicator::getStandardId, id)
        );
        if (indicatorCount == 0) {
            throw new ServiceException(ApiResult.CODE_BAD_REQUEST, "标准下必须配置至少一个指标才能发布");
        }

        // 发布标准
        standard.setStatus("PUBLISHED");
        qualityStandardMapper.updateById(standard);

        // 查询同体系其他已发布的标准（作为提示信息返回）
        List<QcQualityStandard> otherPublished = qualityStandardMapper.findOtherPublishedInSameScope(
                standard.getStandardType(),
                standard.getVariety(),
                standard.getGrade(),
                standard.getCustomerId(),
                id
        );

        List<Map<String, Object>> needingExpiryList = otherPublished.stream().map(s -> {
            Map<String, Object> item = new HashMap<>();
            item.put("standardId", s.getId());
            item.put("versionNo", s.getVersionNo());
            item.put("effectiveDate", s.getEffectiveDate());
            item.put("expiryDate", s.getExpiryDate());
            return item;
        }).collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("standardId", id);
        result.put("needingExpiryStandards", needingExpiryList);
        result.put("message", needingExpiryList.isEmpty()
                ? "标准发布成功"
                : "标准发布成功，以下同体系已发布版本请注意处理有效期：" + needingExpiryList.size() + "条");

        log.info("发布质量标准，id={}, otherPublished={}", id, otherPublished.size());
        return result;
    }

    @Override
    public IPage<QcQualityStandardVO> page(QcQualityStandardPageQuery query) {
        Page<QcQualityStandard> pageParam = new Page<>(query.getPageNum(), query.getPageSize());

        LambdaQueryWrapper<QcQualityStandard> wrapper = new LambdaQueryWrapper<QcQualityStandard>()
                .eq(StringUtils.hasText(query.getStandardType()), QcQualityStandard::getStandardType, query.getStandardType())
                .eq(StringUtils.hasText(query.getVariety()), QcQualityStandard::getVariety, query.getVariety())
                .eq(StringUtils.hasText(query.getGrade()), QcQualityStandard::getGrade, query.getGrade())
                .eq(StringUtils.hasText(query.getStatus()), QcQualityStandard::getStatus, query.getStatus())
                .orderByDesc(QcQualityStandard::getCreateDateTime);

        IPage<QcQualityStandard> standardPage = qualityStandardMapper.selectPage(pageParam, wrapper);

        // 批量查询每个标准的指标数量，转换为VO
        Page<QcQualityStandardVO> voPage = new Page<>(standardPage.getCurrent(), standardPage.getSize(), standardPage.getTotal());
        List<QcQualityStandardVO> voList = standardPage.getRecords().stream().map(s -> {
            QcQualityStandardVO vo = new QcQualityStandardVO();
            vo.setId(s.getId());
            vo.setStandardType(s.getStandardType());
            vo.setVariety(s.getVariety());
            vo.setProductVariety(s.getVariety());
            vo.setGrade(s.getGrade());
            vo.setProductGrade(s.getGrade());
            vo.setSpecRange(s.getSpecRange());
            vo.setVersionNo(s.getVersionNo());
            vo.setVersion(s.getVersionNo());
            vo.setEffectiveDate(s.getEffectiveDate());
            vo.setExpiryDate(s.getExpiryDate());
            vo.setStatus(s.getStatus());
            vo.setCustomerId(s.getCustomerId());
            // 查询指标数量
            long count = standardIndicatorMapper.selectCount(
                    new LambdaQueryWrapper<QcStandardIndicator>()
                            .eq(QcStandardIndicator::getStandardId, s.getId())
            );
            vo.setIndicatorCount(Math.toIntExact(count));
            vo.setRemark(s.getRemark());
            vo.setDescription(s.getRemark());
            return vo;
        }).collect(Collectors.toList());

        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public QcQualityStandardDetailVO getById(String id) {
        QcQualityStandard standard = qualityStandardMapper.selectById(id);
        if (standard == null) {
            throw new ServiceException(ApiResult.CODE_NOT_FOUND, "质量标准不存在");
        }

        // 查询指标配置列表
        List<QcStandardIndicator> siList = standardIndicatorMapper.findByStandardId(id);

        // 查询所有指标元信息
        List<String> indicatorIds = siList.stream()
                .map(QcStandardIndicator::getIndicatorId)
                .collect(Collectors.toList());
        Map<String, QcIndicatorItem> indicatorMap = new HashMap<>();
        if (!indicatorIds.isEmpty()) {
            indicatorItemMapper.selectBatchIds(indicatorIds)
                    .forEach(item -> indicatorMap.put(item.getId(), item));
        }

        // 构建DetailVO
        QcQualityStandardDetailVO detailVO = new QcQualityStandardDetailVO();
        detailVO.setId(standard.getId());
        detailVO.setStandardType(standard.getStandardType());
        detailVO.setVariety(standard.getVariety());
        detailVO.setProductVariety(standard.getVariety());
        detailVO.setGrade(standard.getGrade());
        detailVO.setProductGrade(standard.getGrade());
        detailVO.setSpecRange(standard.getSpecRange());
        detailVO.setVersionNo(standard.getVersionNo());
        detailVO.setVersion(standard.getVersionNo());
        detailVO.setRemark(standard.getRemark());
        detailVO.setDescription(standard.getRemark());
        detailVO.setEffectiveDate(standard.getEffectiveDate());
        detailVO.setExpiryDate(standard.getExpiryDate());
        detailVO.setStatus(standard.getStatus());
        detailVO.setCustomerId(standard.getCustomerId());
        detailVO.setIndicatorCount(siList.size());

        List<QcQualityStandardDetailVO.StandardIndicatorDetail> indicatorDetails = siList.stream().map(si -> {
            QcQualityStandardDetailVO.StandardIndicatorDetail detail = new QcQualityStandardDetailVO.StandardIndicatorDetail();
            detail.setIndicatorId(si.getIndicatorId());
            detail.setUpperLimit(si.getUpperLimit());
            detail.setLowerLimit(si.getLowerLimit());
            detail.setIsRequired(si.getIsRequired());
            detail.setConcessionUpper(si.getConcessionUpper());
            detail.setConcessionLower(si.getConcessionLower());
            QcIndicatorItem item = indicatorMap.get(si.getIndicatorId());
            if (item != null) {
                detail.setIndicatorName(item.getIndicatorName());
                detail.setUnit(item.getUnit());
            }
            return detail;
        }).collect(Collectors.toList());

        detailVO.setIndicators(indicatorDetails);
        return detailVO;
    }

    @Override
    public List<QcIndicatorItemVO> listIndicators(String keyword, String category) {
        return indicatorService.listForSelect(keyword, category);
    }

    /**
     * 批量保存标准指标配置
     */
    private void saveStandardIndicators(String standardId, List<QcQualityStandardAddCmd.StandardIndicatorItem> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        for (QcQualityStandardAddCmd.StandardIndicatorItem item : items) {
            QcStandardIndicator si = new QcStandardIndicator();
            si.setStandardId(standardId);
            si.setIndicatorId(item.getIndicatorId());
            si.setUpperLimit(item.getUpperLimit());
            si.setLowerLimit(item.getLowerLimit());
            si.setIsRequired(item.getIsRequired() != null ? item.getIsRequired() : 1);
            si.setConcessionUpper(item.getConcessionUpper());
            si.setConcessionLower(item.getConcessionLower());
            standardIndicatorMapper.insert(si);
        }
    }
}
