package com.jhict.quality.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jhict.quality.common.annotation.AuditLog;
import com.jhict.quality.common.entity.ApiResult;
import com.jhict.quality.common.exception.ServiceException;
import com.jhict.quality.dto.QcQualityStandardAddCmd;
import com.jhict.quality.dto.QcQualityStandardPageQuery;
import com.jhict.quality.dto.StandardCandidateQuery;
import com.jhict.quality.engine.standard.StandardCompareUtils;
import com.jhict.quality.entity.QcIndicatorItem;
import com.jhict.quality.entity.QcQualityStandard;
import com.jhict.quality.entity.QcStandardIndicator;
import com.jhict.quality.mapper.QcIndicatorItemMapper;
import com.jhict.quality.mapper.QcQualityStandardMapper;
import com.jhict.quality.mapper.QcStandardIndicatorMapper;
import com.jhict.quality.service.api.IndicatorService;
import com.jhict.quality.service.api.StandardDocumentService;
import com.jhict.quality.service.api.StandardService;
import com.jhict.quality.vo.QcIndicatorItemVO;
import com.jhict.quality.vo.QcQualityStandardDetailVO;
import com.jhict.quality.vo.QcQualityStandardVO;
import com.jhict.quality.vo.StandardCandidateSetVO;
import com.jhict.quality.vo.StandardCandidateVO;
import com.jhict.quality.vo.StandardConflictDraftVO;
import com.jhict.quality.vo.StandardDocumentIngestVO;
import com.jhict.quality.vo.StandardSourceDocumentVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class StandardServiceImpl implements StandardService {

    private static final String LEVEL_PRIORITY_RESOLVABLE = "PRIORITY_RESOLVABLE";
    private static final String LEVEL_BLOCKING = "BLOCKING";
    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_RESOLVED = "RESOLVED";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Resource
    private QcQualityStandardMapper qualityStandardMapper;

    @Resource
    private QcStandardIndicatorMapper standardIndicatorMapper;

    @Resource
    private QcIndicatorItemMapper indicatorItemMapper;

    @Resource
    private IndicatorService indicatorService;

    @Resource
    private StandardDocumentService standardDocumentService;

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

        QcQualityStandard standard = new QcQualityStandard();
        applyCmdToEntity(standard, cmd);
        standard.setStatus("DRAFT");
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
        String status = existing.getStatus();
        if (!"DRAFT".equals(status) && !"PUBLISHED".equals(status)) {
            throw new ServiceException(ApiResult.CODE_BAD_REQUEST, "仅草稿或已发布状态的标准可以修改");
        }

        LocalDate expiryDate = cmd.getExpiryDate() != null ? cmd.getExpiryDate() : DEFAULT_EXPIRY_DATE;
        // 校验日期
        if (!cmd.getEffectiveDate().isBefore(expiryDate)) {
            throw new ServiceException(ApiResult.CODE_BAD_REQUEST, "生效日期必须早于失效日期");
        }

        // 客户协议标准校验
        if ("CUSTOMER".equals(cmd.getStandardType()) && !StringUtils.hasText(cmd.getCustomerId())) {
            throw new ServiceException(ApiResult.CODE_BAD_REQUEST, "客户协议标准必须填写客户ID");
        }

        if (cmd.getIndicators() == null || cmd.getIndicators().isEmpty()) {
            throw new ServiceException(ApiResult.CODE_BAD_REQUEST, "标准下必须配置至少一个指标");
        }

        // 已发布标准：保存时校验同体系时间窗口不重叠
        if ("PUBLISHED".equals(status)) {
            List<QcQualityStandard> overlapping = qualityStandardMapper.findOverlappingPublished(
                    cmd.getStandardType(),
                    cmd.getVariety(),
                    cmd.getGrade(),
                    cmd.getCustomerId(),
                    cmd.getEffectiveDate(),
                    expiryDate,
                    cmd.getId()
            );
            if (!overlapping.isEmpty()) {
                QcQualityStandard conflict = overlapping.get(0);
                throw new ServiceException(ApiResult.CODE_BAD_REQUEST,
                        String.format("标准时间窗口与 %s 重叠，请调整生效/失效日期", conflict.getVersionNo()));
            }
        }

        applyCmdToEntity(existing, cmd);
        qualityStandardMapper.updateById(existing);

        // 删除旧指标配置，重新保存
        standardIndicatorMapper.delete(
                new LambdaQueryWrapper<QcStandardIndicator>()
                        .eq(QcStandardIndicator::getStandardId, cmd.getId())
        );
        saveStandardIndicators(cmd.getId(), cmd.getIndicators());

        standardDocumentService.syncLinkedDocument(existing);

        log.info("更新质量标准，id={}", cmd.getId());
    }

    @Override
    @AuditLog(operationType = "DELETE_STANDARD", targetEntity = "QcQualityStandard")
    @Transactional(rollbackFor = Exception.class)
    public void deleteStandard(String id) {
        QcQualityStandard existing = qualityStandardMapper.selectById(id);
        if (existing == null) {
            throw new ServiceException(ApiResult.CODE_NOT_FOUND, "质量标准不存在");
        }

        standardIndicatorMapper.delete(
                new LambdaQueryWrapper<QcStandardIndicator>()
                        .eq(QcStandardIndicator::getStandardId, id)
        );
        standardDocumentService.removeLinkedDocument(id);
        qualityStandardMapper.deleteById(id);

        log.info("删除质量标准，id={}, type={}, variety={}, grade={}",
                id, existing.getStandardType(), existing.getVariety(), existing.getGrade());
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

        // 时间窗口重叠校验：同体系已发布标准生效区间不得重叠（FR-003 / TC-B004）
        List<QcQualityStandard> overlapping = qualityStandardMapper.findOverlappingPublished(
                standard.getStandardType(),
                standard.getVariety(),
                standard.getGrade(),
                standard.getCustomerId(),
                standard.getEffectiveDate(),
                standard.getExpiryDate(),
                id
        );
        if (!overlapping.isEmpty()) {
            QcQualityStandard conflict = overlapping.get(0);
            throw new ServiceException(ApiResult.CODE_BAD_REQUEST,
                    String.format("标准时间窗口与 %s 重叠，请调整生效/失效日期", conflict.getVersionNo()));
        }

        // 发布标准
        standard.setStatus("PUBLISHED");
        qualityStandardMapper.updateById(standard);

        standardDocumentService.syncLinkedDocument(standard);

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

        StandardSourceDocumentVO sourceDocument = standardDocumentService.buildSourceDocumentSummary(id);
        result.put("sourceDocument", sourceDocument);
        if (Boolean.TRUE.equals(sourceDocument.getHasSourceFile())) {
            try {
                StandardDocumentIngestVO ingestVO = standardDocumentService.ingestLinkedDocument(id);
                result.put("sourceIngest", ingestVO);
            } catch (Exception ex) {
                log.warn("标准发布后立即索引源 PDF 失败，standardId={}, error={}", id, ex.getMessage());
                result.put("sourceIngestError", ex.getMessage());
            }
        }

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
            vo.setStandardCode(s.getStandardCode());
            vo.setStandardName(s.getStandardName());
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
        detailVO.setStandardCode(standard.getStandardCode());
        detailVO.setStandardName(standard.getStandardName());
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
                detail.setIndicatorCode(item.getIndicatorCode());
                detail.setCategory(item.getIndicatorCategory());
                detail.setUnit(item.getUnit());
            }
            return detail;
        }).collect(Collectors.toList());

        detailVO.setIndicators(indicatorDetails);
        detailVO.setSourceDocument(standardDocumentService.buildSourceDocumentSummary(id));
        return detailVO;
    }

    @Override
    public List<QcIndicatorItemVO> listIndicators(String keyword, String category) {
        return indicatorService.listForSelect(keyword, category);
    }

    @Override
    public StandardCandidateSetVO findCandidateStandards(StandardCandidateQuery query) {
        List<QcQualityStandard> standards = qualityStandardMapper.findApplicableCandidateStandards(
                query.getCustomerId(),
                query.getVariety(),
                query.getGrade(),
                query.getTestDate()
        );

        List<StandardCandidateVO> candidates = standards.stream()
                .map(this::toCandidateVO)
                .collect(Collectors.toList());

        StandardCandidateVO provisionalSelected = candidates.isEmpty() ? null : candidates.get(0);
        List<StandardConflictDraftVO> conflicts = detectCandidateConflicts(query, standards, provisionalSelected);
        boolean hasBlockingConflict = conflicts.stream()
                .anyMatch(conflict -> LEVEL_BLOCKING.equals(conflict.getConflictLevel()));
        StandardCandidateVO selected = hasBlockingConflict ? null : provisionalSelected;

        List<StandardCandidateVO> suppressed = new ArrayList<>();
        List<StandardCandidateVO> conflictStandards = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        Set<String> conflictStandardIds = conflicts.stream()
                .filter(conflict -> conflict.getInvolvedStandardIds() != null)
                .flatMap(conflict -> conflict.getInvolvedStandardIds().stream())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        for (StandardCandidateVO candidate : candidates) {
            boolean isSelected = selected != null && selected.getId().equals(candidate.getId());
            candidate.setSelected(isSelected);
            candidate.setConflict(conflictStandardIds.contains(candidate.getId()));
            if (isSelected) {
                candidate.setSuppressed(false);
                candidate.setReason(candidate.getConflict()
                        ? "按客户协议 > 企标 > 国标优先级选择，并存在优先级可解差异提示"
                        : "按客户协议 > 企标 > 国标优先级选择");
            } else {
                candidate.setSuppressed(selected != null);
                if (selected != null) {
                    candidate.setReason(candidate.getConflict()
                            ? "存在更高优先级适用标准，当前标准被抑制，并保留优先级可解差异提示"
                            : "存在更高优先级适用标准，当前标准被抑制");
                    suppressed.add(candidate);
                } else if (candidate.getConflict()) {
                    candidate.setReason("候选标准参与阻断冲突，需要先完成人工裁决");
                } else {
                    candidate.setReason("未选中");
                }
            }
            if (candidate.getConflict()) {
                conflictStandards.add(candidate);
            }
        }

        if (selected == null) {
            if (hasBlockingConflict) {
                warnings.add("存在未裁决的同优先级阻断标准冲突，禁止直接给出放行类判定");
            } else {
                warnings.add("未找到覆盖当前客户/品种/牌号/检验日期的已发布标准");
            }
        }
        if (StringUtils.hasText(query.getProductSpec())) {
            warnings.add("规格范围已参与确定性重叠判断，无法解析的文本规格会按需人工复核");
        }
        if (query.getIndicatorIds() == null || query.getIndicatorIds().isEmpty()) {
            warnings.add("未传入指标ID，候选集仅按客户、品种、牌号和生效期匹配");
        }
        if (conflicts.stream().anyMatch(conflict -> LEVEL_PRIORITY_RESOLVABLE.equals(conflict.getConflictLevel()))) {
            warnings.add("存在优先级可解标准差异，系统按最高优先级标准继续判定并保留风险提示");
        }

        StandardCandidateSetVO result = new StandardCandidateSetVO();
        result.setSelectedStandard(selected);
        result.setCandidateStandards(candidates);
        result.setSuppressedStandards(suppressed);
        result.setConflictStandards(conflictStandards);
        result.setConflicts(conflicts);
        result.setWarnings(warnings);
        return result;
    }

    private List<StandardConflictDraftVO> detectCandidateConflicts(StandardCandidateQuery query,
                                                                  List<QcQualityStandard> standards,
                                                                  StandardCandidateVO selected) {
        List<StandardConflictDraftVO> conflicts = new ArrayList<>();
        if (standards == null || standards.size() < 2) {
            return conflicts;
        }
        List<String> standardIds = standards.stream().map(QcQualityStandard::getId).collect(Collectors.toList());
        Map<String, QcQualityStandard> standardMap = standards.stream()
                .collect(Collectors.toMap(QcQualityStandard::getId, s -> s));
        Map<String, List<QcStandardIndicator>> rulesByStandard = loadRulesByStandard(standardIds, query.getIndicatorIds());
        Set<String> inspectedIndicatorIds = query.getIndicatorIds() == null
                ? new HashSet<>()
                : new HashSet<>(query.getIndicatorIds());
        Set<String> allIndicatorIds = rulesByStandard.values().stream()
                .flatMap(Collection::stream)
                .map(QcStandardIndicator::getIndicatorId)
                .collect(Collectors.toSet());
        Map<String, QcIndicatorItem> indicatorMap = indicatorService.listEntitiesByIds(allIndicatorIds).stream()
                .collect(Collectors.toMap(QcIndicatorItem::getId, item -> item));

        Map<Integer, List<QcQualityStandard>> byPriority = standards.stream()
                .collect(Collectors.groupingBy(s -> resolveStandardPriority(s.getStandardType())));
        for (List<QcQualityStandard> samePriorityStandards : byPriority.values()) {
            if (samePriorityStandards.size() < 2) {
                continue;
            }
            conflicts.addAll(compareSamePriorityConflicts(query, samePriorityStandards, rulesByStandard,
                    indicatorMap, inspectedIndicatorIds));
        }

        boolean hasBlocking = conflicts.stream().anyMatch(c -> LEVEL_BLOCKING.equals(c.getConflictLevel()));
        if (selected != null && !hasBlocking) {
            QcQualityStandard selectedStandard = standardMap.get(selected.getId());
            for (QcQualityStandard candidate : standards) {
                if (candidate.getId().equals(selected.getId())
                        || resolveStandardPriority(candidate.getStandardType()) <= selected.getPriority()) {
                    continue;
                }
                conflicts.addAll(comparePriorityResolvableConflicts(query, selectedStandard, candidate,
                        rulesByStandard, indicatorMap, inspectedIndicatorIds));
            }
        }
        return conflicts;
    }

    private Map<String, List<QcStandardIndicator>> loadRulesByStandard(List<String> standardIds, List<String> indicatorIds) {
        if (standardIds == null || standardIds.isEmpty()) {
            return new HashMap<>();
        }
        LambdaQueryWrapper<QcStandardIndicator> wrapper = new LambdaQueryWrapper<QcStandardIndicator>()
                .in(QcStandardIndicator::getStandardId, standardIds);
        return standardIndicatorMapper.selectList(wrapper).stream()
                .collect(Collectors.groupingBy(QcStandardIndicator::getStandardId));
    }

    private List<StandardConflictDraftVO> compareSamePriorityConflicts(StandardCandidateQuery query,
                                                                       List<QcQualityStandard> standards,
                                                                       Map<String, List<QcStandardIndicator>> rulesByStandard,
                                                                       Map<String, QcIndicatorItem> indicatorMap,
                                                                       Set<String> inspectedIndicatorIds) {
        List<StandardConflictDraftVO> conflicts = new ArrayList<>();
        for (int i = 0; i < standards.size(); i++) {
            for (int j = i + 1; j < standards.size(); j++) {
                QcQualityStandard left = standards.get(i);
                QcQualityStandard right = standards.get(j);
                List<QcStandardIndicator> leftRules = rulesByStandard.getOrDefault(left.getId(), new ArrayList<>());
                List<QcStandardIndicator> rightRules = rulesByStandard.getOrDefault(right.getId(), new ArrayList<>());
                for (QcStandardIndicator leftRule : leftRules) {
                    for (QcStandardIndicator rightRule : rightRules) {
                        if (!shouldCompareRulePair(leftRule, rightRule, indicatorMap, inspectedIndicatorIds)) {
                            continue;
                        }
                        QcIndicatorItem leftIndicator = indicatorMap.get(leftRule.getIndicatorId());
                        QcIndicatorItem rightIndicator = indicatorMap.get(rightRule.getIndicatorId());
                        StandardCompareUtils.RuleCompatibility result = StandardCompareUtils.compareRules(
                                left, leftRule, leftIndicator, right, rightRule, rightIndicator);
                        if (result.isBlockingConflict()) {
                            conflicts.add(toConflictDraft(query, left, right, leftRule, rightRule, leftIndicator,
                                    result.getConflictType(), LEVEL_BLOCKING, STATUS_PENDING, null,
                                    left.getStandardType(), result.getMessage()));
                        }
                    }
                }
            }
        }
        return conflicts;
    }

    private List<StandardConflictDraftVO> comparePriorityResolvableConflicts(StandardCandidateQuery query,
                                                                             QcQualityStandard selectedStandard,
                                                                             QcQualityStandard suppressedStandard,
                                                                             Map<String, List<QcStandardIndicator>> rulesByStandard,
                                                                             Map<String, QcIndicatorItem> indicatorMap,
                                                                             Set<String> inspectedIndicatorIds) {
        List<StandardConflictDraftVO> conflicts = new ArrayList<>();
        if (selectedStandard == null || suppressedStandard == null) {
            return conflicts;
        }
        List<QcStandardIndicator> selectedRules = rulesByStandard.getOrDefault(selectedStandard.getId(), new ArrayList<>());
        List<QcStandardIndicator> suppressedRules = rulesByStandard.getOrDefault(suppressedStandard.getId(), new ArrayList<>());
        for (QcStandardIndicator selectedRule : selectedRules) {
            for (QcStandardIndicator suppressedRule : suppressedRules) {
                if (!shouldCompareRulePair(selectedRule, suppressedRule, indicatorMap, inspectedIndicatorIds)) {
                    continue;
                }
                QcIndicatorItem selectedIndicator = indicatorMap.get(selectedRule.getIndicatorId());
                QcIndicatorItem suppressedIndicator = indicatorMap.get(suppressedRule.getIndicatorId());
                if (!StandardCompareUtils.comparableIndicator(selectedRule, selectedIndicator, suppressedRule, suppressedIndicator)
                        || !StandardCompareUtils.comparableUnit(selectedIndicator == null ? null : selectedIndicator.getUnit(),
                        suppressedIndicator == null ? null : suppressedIndicator.getUnit())
                        || !StandardCompareUtils.specRangesOverlap(selectedStandard.getSpecRange(), suppressedStandard.getSpecRange())) {
                    continue;
                }
                if (!StandardCompareUtils.numericLimitsEqual(selectedRule, suppressedRule)) {
                    conflicts.add(toConflictDraft(query, selectedStandard, suppressedStandard, selectedRule,
                            suppressedRule, selectedIndicator, StandardCompareUtils.CONFLICT_NUMERIC_LIMIT,
                            LEVEL_PRIORITY_RESOLVABLE, STATUS_RESOLVED, selectedStandard.getId(),
                            selectedStandard.getStandardType(), "跨优先级适用标准限值不同，按最高优先级标准继续判定"));
                }
            }
        }
        return conflicts;
    }

    private boolean shouldCompareRulePair(QcStandardIndicator leftRule,
                                          QcStandardIndicator rightRule,
                                          Map<String, QcIndicatorItem> indicatorMap,
                                          Set<String> inspectedIndicatorIds) {
        if (leftRule == null || rightRule == null) {
            return false;
        }
        if (inspectedIndicatorIds != null && !inspectedIndicatorIds.isEmpty()
                && !inspectedIndicatorIds.contains(leftRule.getIndicatorId())
                && !inspectedIndicatorIds.contains(rightRule.getIndicatorId())) {
            return false;
        }
        QcIndicatorItem leftIndicator = indicatorMap.get(leftRule.getIndicatorId());
        QcIndicatorItem rightIndicator = indicatorMap.get(rightRule.getIndicatorId());
        return StandardCompareUtils.comparableIndicator(leftRule, leftIndicator, rightRule, rightIndicator);
    }

    private StandardConflictDraftVO toConflictDraft(StandardCandidateQuery query,
                                                   QcQualityStandard leftStandard,
                                                   QcQualityStandard rightStandard,
                                                   QcStandardIndicator leftRule,
                                                   QcStandardIndicator rightRule,
                                                   QcIndicatorItem indicator,
                                                   String conflictType,
                                                   String conflictLevel,
                                                   String status,
                                                   String selectedStandardId,
                                                   String selectedPriority,
                                                   String message) {
        StandardConflictDraftVO draft = new StandardConflictDraftVO();
        draft.setConflictType(conflictType);
        draft.setConflictLevel(conflictLevel);
        draft.setStatus(status);
        draft.setIndicatorId(leftRule.getIndicatorId());
        draft.setIndicatorName(indicator == null ? leftRule.getIndicatorId() : indicator.getIndicatorName());
        draft.setUnit(indicator == null ? null : indicator.getUnit());
        draft.setCustomerId(query.getCustomerId());
        draft.setVariety(query.getVariety());
        draft.setGrade(query.getGrade());
        draft.setProductSpec(query.getProductSpec());
        draft.setInspectionDate(query.getTestDate());
        draft.setSelectedStandardId(selectedStandardId);
        draft.setInvolvedStandardIds(Arrays.asList(leftStandard.getId(), rightStandard.getId()));
        draft.setConflictDetail(buildConflictDetail(leftStandard, rightStandard, leftRule, rightRule, message));
        draft.setSelectedPriority(selectedPriority);
        draft.setMessage(message);
        return draft;
    }

    private String buildConflictDetail(QcQualityStandard leftStandard,
                                       QcQualityStandard rightStandard,
                                       QcStandardIndicator leftRule,
                                       QcStandardIndicator rightRule,
                                       String message) {
        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("reason", message);
        detail.put("leftStandard", standardRuleSnapshot(leftStandard, leftRule));
        detail.put("rightStandard", standardRuleSnapshot(rightStandard, rightRule));
        try {
            return objectMapper.writeValueAsString(detail);
        } catch (Exception e) {
            log.warn("序列化标准冲突明细失败，leftStandardId={}, rightStandardId={}",
                    leftStandard.getId(), rightStandard.getId());
            return "{\"reason\":\"" + message + "\"}";
        }
    }

    private Map<String, Object> standardRuleSnapshot(QcQualityStandard standard, QcStandardIndicator rule) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("standardId", standard.getId());
        snapshot.put("standardType", standard.getStandardType());
        snapshot.put("standardCode", standard.getStandardCode());
        snapshot.put("versionNo", standard.getVersionNo());
        snapshot.put("specRange", standard.getSpecRange());
        snapshot.put("effectiveDate", standard.getEffectiveDate() == null ? null : standard.getEffectiveDate().toString());
        snapshot.put("expiryDate", standard.getExpiryDate() == null ? null : standard.getExpiryDate().toString());
        snapshot.put("indicatorId", rule.getIndicatorId());
        snapshot.put("lowerLimit", rule.getLowerLimit());
        snapshot.put("upperLimit", rule.getUpperLimit());
        snapshot.put("concessionLower", rule.getConcessionLower());
        snapshot.put("concessionUpper", rule.getConcessionUpper());
        return snapshot;
    }

    private static final LocalDate DEFAULT_EXPIRY_DATE = LocalDate.of(9999, 12, 31);

    private void applyCmdToEntity(QcQualityStandard standard, QcQualityStandardAddCmd cmd) {
        standard.setStandardType(cmd.getStandardType());
        standard.setStandardCode(cmd.getStandardCode());
        standard.setStandardName(cmd.getStandardName());
        standard.setVariety(cmd.getVariety());
        standard.setGrade(cmd.getGrade());
        standard.setSpecRange(cmd.getSpecRange());
        standard.setVersionNo(cmd.getVersionNo());
        standard.setEffectiveDate(cmd.getEffectiveDate());
        standard.setExpiryDate(cmd.getExpiryDate() != null ? cmd.getExpiryDate() : DEFAULT_EXPIRY_DATE);
        standard.setCustomerId(cmd.getCustomerId());
        standard.setRemark(cmd.getRemark());
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

    private StandardCandidateVO toCandidateVO(QcQualityStandard standard) {
        StandardCandidateVO vo = new StandardCandidateVO();
        vo.setId(standard.getId());
        vo.setStandardType(standard.getStandardType());
        vo.setStandardCode(standard.getStandardCode());
        vo.setStandardName(standard.getStandardName());
        vo.setVersionNo(standard.getVersionNo());
        vo.setCustomerId(standard.getCustomerId());
        vo.setVariety(standard.getVariety());
        vo.setGrade(standard.getGrade());
        vo.setSpecRange(standard.getSpecRange());
        vo.setEffectiveDate(standard.getEffectiveDate());
        vo.setExpiryDate(standard.getExpiryDate());
        vo.setPriority(resolveStandardPriority(standard.getStandardType()));
        vo.setSelected(false);
        vo.setSuppressed(false);
        vo.setConflict(false);
        return vo;
    }

    private int resolveStandardPriority(String standardType) {
        if ("CUSTOMER".equals(standardType)) {
            return 1;
        }
        if ("ENTERPRISE".equals(standardType)) {
            return 2;
        }
        if ("NATIONAL".equals(standardType)) {
            return 3;
        }
        return 9;
    }
}
