package com.jhict.quality.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jhict.quality.common.constant.JudgmentExplainConstants;
import com.jhict.quality.common.entity.ApiResult;
import com.jhict.quality.common.exception.ServiceException;
import com.jhict.quality.enums.StandardType;
import com.jhict.quality.dto.QcJudgmentPageQuery;
import com.jhict.quality.engine.model.JudgmentOutput;
import com.jhict.quality.entity.*;
import com.jhict.quality.mapper.*;
import com.jhict.quality.ai.conflict.StandardConflictDetector;
import com.jhict.quality.service.api.JudgmentService;
import com.jhict.quality.service.api.NotificationService;
import com.jhict.quality.entity.SysUser;
import com.jhict.quality.mapper.SysUserMapper;
import com.jhict.quality.vo.DashboardSummaryVO;
import com.jhict.quality.vo.QcJudgmentListVO;
import com.jhict.quality.vo.QcJudgmentResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class JudgmentServiceImpl implements JudgmentService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String DASHBOARD_CACHE_KEY_PREFIX = "dashboard:summary:";
    private static final String DEFAULT_COMPANY_ID = "DEFAULT";
    private static final long DASHBOARD_TTL_SECONDS = 60L;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Resource
    private QcJudgmentResultMapper judgmentResultMapper;

    @Resource
    private QcJudgmentEvidenceMapper judgmentEvidenceMapper;

    @Resource
    private StandardGapMapper standardGapMapper;

    @Resource
    private QcIndicatorItemMapper indicatorItemMapper;

    @Resource
    private QcQualityStandardMapper qualityStandardMapper;

    @Resource
    private QcStandardIndicatorMapper standardIndicatorMapper;

    @Resource
    private QcInspectionRecordMapper inspectionRecordMapper;

    @Resource
    private SysUserMapper sysUserMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private StandardConflictDetector standardConflictDetector;

    /** 懒注入，避免循环依赖 */
    @Lazy
    @Autowired
    private NotificationService notificationService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public QcJudgmentResult saveJudgmentResult(String recordId, JudgmentOutput output) {
        // Step 1: 保存JudgmentResult
        QcJudgmentResult judgmentResult = new QcJudgmentResult();
        judgmentResult.setRecordId(recordId);
        judgmentResult.setJudgmentType(output.getJudgmentType().getCode());
        judgmentResult.setJudgmentTime(LocalDateTime.now());
        judgmentResult.setIsFinal(1);

        // 将matchedStandardIds序列化为JSON字符串
        String matchedStandardIdsJson = "[]";
        if (output.getMatchedStandardIds() != null && !output.getMatchedStandardIds().isEmpty()) {
            try {
                matchedStandardIdsJson = objectMapper.writeValueAsString(output.getMatchedStandardIds());
            } catch (Exception e) {
                log.warn("序列化matchedStandardIds失败，使用默认值[]", e);
            }
        }
        judgmentResult.setMatchedStandardIds(matchedStandardIdsJson);
        judgmentResultMapper.insert(judgmentResult);

        String judgmentId = judgmentResult.getId();

        // Step 2: 批量保存JudgmentEvidence（快照）
        if (output.getEvidences() != null && !output.getEvidences().isEmpty()) {
            for (JudgmentOutput.EvidenceItem evidenceItem : output.getEvidences()) {
                QcJudgmentEvidence evidence = new QcJudgmentEvidence();
                evidence.setJudgmentId(judgmentId);
                evidence.setStandardId(evidenceItem.getStandardId());
                evidence.setIndicatorId(evidenceItem.getIndicatorId());
                evidence.setTestValue(evidenceItem.getTestValue());
                evidence.setUpperLimit(evidenceItem.getUpperLimit());
                evidence.setLowerLimit(evidenceItem.getLowerLimit());
                evidence.setDeviation(evidenceItem.getDeviation());
                evidence.setTriggerRule(evidenceItem.getTriggerRule());
                evidence.setIsPassed(evidenceItem.isPassed() ? 1 : 0);
                judgmentEvidenceMapper.insert(evidence);
            }
        }

        // Step 3: 保存StandardGap（仅保存新缺口，已存在的不重复插入）
        if (output.getGaps() != null && !output.getGaps().isEmpty()) {
            for (JudgmentOutput.StandardGapItem gapItem : output.getGaps()) {
                StandardGap existingGap = standardGapMapper.findUnresolvedGap(
                        gapItem.getVariety(), gapItem.getGrade(), gapItem.getIndicatorId());
                if (existingGap == null) {
                    // 新缺口，插入记录
                    StandardGap gap = new StandardGap();
                    gap.setVariety(gapItem.getVariety());
                    gap.setGrade(gapItem.getGrade());
                    gap.setIndicatorId(gapItem.getIndicatorId());
                    gap.setFirstFoundTime(LocalDateTime.now());
                    gap.setRelatedRecordId(recordId);
                    gap.setIsResolved(0);
                    standardGapMapper.insert(gap);
                    log.info("发现新标准缺口，variety={}, grade={}, indicatorId={}",
                            gapItem.getVariety(), gapItem.getGrade(), gapItem.getIndicatorId());
                    // 发送标准缺口通知给质量工程师（SYSTEM通知）
                    try {
                        // 查询指标名称用于通知内容
                        QcIndicatorItem gapIndicator = indicatorItemMapper.selectById(gapItem.getIndicatorId());
                        String indicatorName = gapIndicator != null ? gapIndicator.getIndicatorName() : gapItem.getIndicatorId();
                        notificationService.send(
                                "SYSTEM",
                                "发现新标准缺口",
                                String.format("品种[%s]牌号[%s]的指标[%s]在现有标准中无覆盖，请及时补充标准配置",
                                        gapItem.getVariety(), gapItem.getGrade(), indicatorName),
                                "STANDARD_GAP",
                                gap.getId()
                        );
                    } catch (Exception e) {
                        log.warn("发送标准缺口通知失败，不影响业务，error={}", e.getMessage());
                    }
                }
            }
        }

        // Step 4: 冲突旁路写入（多标准命中时检测同指标限值冲突）
        runConflictDetection(recordId, output.getMatchedStandardIds());

        log.info("保存判定结论成功，judgmentId={}, recordId={}, type={}",
                judgmentId, recordId, output.getJudgmentType());
        return judgmentResult;
    }

    /**
     * 多标准同指标限值冲突旁路检测：
     * - 仅当命中 ≥2 个标准时触发
     * - 新冲突写入 qc_standard_conflict（PENDING），已存在的跳过
     * - 失败不阻断主判定事务（catch 后 warn 日志）
     */
    private void runConflictDetection(String recordId, List<String> matchedStandardIds) {
        if (matchedStandardIds == null || matchedStandardIds.size() < 2) {
            return;
        }
        try {
            QcInspectionRecord record = inspectionRecordMapper.selectById(recordId);
            if (record == null) {
                log.warn("冲突检测跳过：未找到检验记录 recordId={}", recordId);
                return;
            }
            List<QcStandardConflict> conflicts = standardConflictDetector.detectAndPersist(record, matchedStandardIds);
            if (!conflicts.isEmpty()) {
                log.info("冲突旁路写入完成，recordId={}，检出冲突数={}", recordId, conflicts.size());
            }
        } catch (Exception e) {
            log.warn("冲突旁路检测异常，不影响判定主流程，recordId={}，error={}", recordId, e.getMessage());
        }
    }

    @Override
    public QcJudgmentResultVO getCurrentJudgment(String recordId) {
        QcJudgmentResult result = judgmentResultMapper.findFinalByRecordId(recordId);
        if (result == null) {
            throw new ServiceException(ApiResult.CODE_NOT_FOUND, "未找到该检验记录的最终判定结论");
        }

        QcInspectionRecord record = inspectionRecordMapper.selectById(recordId);
        return buildJudgmentResultVO(result, record, true);
    }

    @Override
    public QcJudgmentResultVO getExplanation(String id) {
        QcJudgmentResult result = judgmentResultMapper.selectById(id);
        if (result == null) {
            throw new ServiceException(ApiResult.CODE_NOT_FOUND, "判定结论不存在");
        }

        QcInspectionRecord record = inspectionRecordMapper.selectById(result.getRecordId());
        return buildJudgmentResultVO(result, record, true);
    }

    @Override
    public IPage<QcJudgmentListVO> page(QcJudgmentPageQuery query) {
        int pageNum = resolvePageNum(query.getPageNum());
        int pageSize = resolvePageSize(query.getPageSize());

        Set<String> recordIdFilter = resolveRecordIdsByCoilOrBatch(query.getCoilNo(), query.getBatchNo());
        if (recordIdFilter != null && recordIdFilter.isEmpty()) {
            return emptyJudgmentListPage(pageNum, pageSize);
        }

        IPage<QcJudgmentResult> entityPage = judgmentResultMapper.selectPage(
                new Page<>(pageNum, pageSize), buildJudgmentPageWrapper(query, recordIdFilter));
        return convertToJudgmentListPage(entityPage);
    }

    @Override
    public DashboardSummaryVO getDashboardSummary() {
        String cacheKey = DASHBOARD_CACHE_KEY_PREFIX + DEFAULT_COMPANY_ID;

        // 尝试从Redis读取缓存
        String cached = null;
        try {
            cached = stringRedisTemplate.opsForValue().get(cacheKey);
        } catch (Exception e) {
            log.warn("读取Redis缓存失败，直接查DB，error={}", e.getMessage());
        }

        if (StringUtils.hasText(cached)) {
            try {
                return objectMapper.readValue(cached, DashboardSummaryVO.class);
            } catch (Exception e) {
                log.warn("反序列化看板缓存失败，重新查DB", e);
            }
        }

        // 缓存未命中，查数据库
        DashboardSummaryVO summary = new DashboardSummaryVO();
        summary.setPendingJudgmentCount(inspectionRecordMapper.countPendingJudgment());
        summary.setUnqualifiedCount(inspectionRecordMapper.countUnqualified());
        summary.setPendingReinspectionCount(inspectionRecordMapper.countPendingReinspection());
        summary.setPendingConcessionApprovalCount(inspectionRecordMapper.countPendingConcessionApproval());
        summary.setCacheUpdatedAt(LocalDateTime.now().format(FORMATTER));

        // 写入Redis缓存，TTL=60s
        try {
            String json = objectMapper.writeValueAsString(summary);
            stringRedisTemplate.opsForValue().set(cacheKey, json, DASHBOARD_TTL_SECONDS, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("写入Redis缓存失败，不影响业务，error={}", e.getMessage());
        }

        return summary;
    }

    /**
     * 构建判定解释 VO（检验信息、标准匹配、指标明细分步填充）
     */
    private QcJudgmentResultVO buildJudgmentResultVO(QcJudgmentResult result, QcInspectionRecord record, boolean loadEvidences) {
        QcJudgmentResultVO vo = new QcJudgmentResultVO();
        vo.setJudgmentId(result.getId());
        vo.setRecordId(result.getRecordId());
        fillInspectionFieldsOnVo(vo, result, record);
        fillStandardFieldsOnVo(vo, result);
        fillEvidenceFieldsOnVo(vo, result, loadEvidences);
        return vo;
    }

    private void fillInspectionFieldsOnVo(QcJudgmentResultVO vo, QcJudgmentResult result, QcInspectionRecord record) {
        if (record == null) {
            return;
        }
        vo.setCoilNo(record.getCoilNo());
        vo.setBatchNo(record.getBatchNo());
        vo.setHeatNo(record.getHeatNo());
        vo.setProductVariety(record.getProductVariety());
        vo.setProductGrade(record.getProductGrade());
        vo.setProductSpec(record.getProductSpec());
        vo.setSpecification(record.getProductSpec());
        vo.setTesterNo(record.getTesterNo());
        if (StringUtils.hasText(record.getTesterNo())) {
            Map<String, String> nameMap = loadUserNameMap(Collections.singleton(record.getTesterNo()));
            vo.setInspector(nameMap.getOrDefault(record.getTesterNo(), record.getTesterNo()));
        }
        String judgmentTimeStr = result.getJudgmentTime() != null
                ? result.getJudgmentTime().format(FORMATTER) : null;
        vo.setJudgmentType(result.getJudgmentType());
        vo.setJudgmentTime(judgmentTimeStr);
        vo.setJudgeTime(judgmentTimeStr);
        vo.setIsFinal(result.getIsFinal());
    }

    private void fillStandardFieldsOnVo(QcJudgmentResultVO vo, QcJudgmentResult result) {
        List<String> standardIds = parseMatchedStandardIds(result.getMatchedStandardIds());
        List<QcQualityStandard> matchedEntityList = standardIds.isEmpty()
                ? Collections.emptyList()
                : qualityStandardMapper.selectBatchIds(standardIds);
        vo.setMatchedStandards(convertToMatchedStandardVoList(matchedEntityList));
        vo.setStandardMatches(buildStandardMatches(matchedEntityList));
    }

    private void fillEvidenceFieldsOnVo(QcJudgmentResultVO vo, QcJudgmentResult result, boolean loadEvidences) {
        if (!loadEvidences) {
            vo.setEvidences(Collections.emptyList());
            vo.setIndicatorDetails(Collections.emptyList());
            return;
        }
        List<QcJudgmentEvidence> evidenceList = judgmentEvidenceMapper.findByJudgmentId(result.getId());
        Map<String, QcIndicatorItem> indicatorMap = loadIndicatorMap(evidenceList);
        vo.setEvidences(convertToEvidenceVoList(evidenceList, indicatorMap));
        Map<String, QcStandardIndicator> standardIndicatorMap = loadStandardIndicatorMap(evidenceList);
        vo.setIndicatorDetails(buildIndicatorDetails(evidenceList, indicatorMap, standardIndicatorMap));
    }

    private List<QcJudgmentResultVO.MatchedStandardVO> convertToMatchedStandardVoList(List<QcQualityStandard> matchedList) {
        return matchedList.stream().map(s -> {
            QcJudgmentResultVO.MatchedStandardVO msVO = new QcJudgmentResultVO.MatchedStandardVO();
            msVO.setStandardId(s.getId());
            msVO.setStandardType(s.getStandardType());
            msVO.setVersionNo(s.getVersionNo());
            msVO.setSpecRange(s.getSpecRange());
            return msVO;
        }).collect(Collectors.toList());
    }

    private List<QcJudgmentResultVO.StandardMatchVO> buildStandardMatches(List<QcQualityStandard> matchedList) {
        Map<String, QcQualityStandard> byType = matchedList.stream()
                .filter(s -> StringUtils.hasText(s.getStandardType()))
                .collect(Collectors.toMap(QcQualityStandard::getStandardType, s -> s, (a, b) -> a));

        List<QcJudgmentResultVO.StandardMatchVO> matches = new ArrayList<>();
        for (StandardType type : JudgmentExplainConstants.STANDARD_MATCH_PRIORITY) {
            QcJudgmentResultVO.StandardMatchVO match = new QcJudgmentResultVO.StandardMatchVO();
            match.setStandardType(type.getCode());
            QcQualityStandard hit = byType.get(type.getCode());
            if (hit != null) {
                match.setHit(Boolean.TRUE);
                String name = StringUtils.hasText(hit.getStandardName())
                        ? hit.getStandardName()
                        : hit.getSpecRange() + " (" + hit.getVersionNo() + ")";
                match.setStandardName(name);
            } else {
                match.setHit(Boolean.FALSE);
                match.setSkipReason(JudgmentExplainConstants.SKIP_REASON_NOT_MATCHED);
            }
            matches.add(match);
        }
        return matches;
    }

    private Map<String, QcIndicatorItem> loadIndicatorMap(List<QcJudgmentEvidence> evidenceList) {
        List<String> indicatorIds = evidenceList.stream()
                .map(QcJudgmentEvidence::getIndicatorId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (indicatorIds.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, QcIndicatorItem> indicatorMap = new HashMap<>();
        indicatorItemMapper.selectBatchIds(indicatorIds)
                .forEach(item -> indicatorMap.put(item.getId(), item));
        return indicatorMap;
    }

    private List<QcJudgmentResultVO.EvidenceVO> convertToEvidenceVoList(
            List<QcJudgmentEvidence> evidenceList, Map<String, QcIndicatorItem> indicatorMap) {
        return evidenceList.stream().map(e -> {
            QcJudgmentResultVO.EvidenceVO evidenceVO = new QcJudgmentResultVO.EvidenceVO();
            evidenceVO.setTestValue(e.getTestValue());
            evidenceVO.setUpperLimit(e.getUpperLimit());
            evidenceVO.setLowerLimit(e.getLowerLimit());
            evidenceVO.setDeviation(e.getDeviation());
            evidenceVO.setTriggerRule(e.getTriggerRule());
            evidenceVO.setIsPassed(e.getIsPassed());
            QcIndicatorItem indicator = indicatorMap.get(e.getIndicatorId());
            if (indicator != null) {
                evidenceVO.setIndicatorName(indicator.getIndicatorName());
                evidenceVO.setIndicatorCode(indicator.getIndicatorCode());
                evidenceVO.setUnit(indicator.getUnit());
            }
            return evidenceVO;
        }).collect(Collectors.toList());
    }

    private Map<String, QcStandardIndicator> loadStandardIndicatorMap(List<QcJudgmentEvidence> evidenceList) {
        Set<String> standardIds = evidenceList.stream()
                .map(QcJudgmentEvidence::getStandardId)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());
        Map<String, QcStandardIndicator> map = new HashMap<>();
        for (String standardId : standardIds) {
            List<QcStandardIndicator> configs = standardIndicatorMapper.findByStandardId(standardId);
            if (configs == null) {
                continue;
            }
            for (QcStandardIndicator si : configs) {
                map.put(standardIndicatorKey(standardId, si.getIndicatorId()), si);
            }
        }
        return map;
    }

    private static String standardIndicatorKey(String standardId, String indicatorId) {
        return standardId + ":" + indicatorId;
    }

    private List<QcJudgmentResultVO.IndicatorDetailVO> buildIndicatorDetails(
            List<QcJudgmentEvidence> evidenceList,
            Map<String, QcIndicatorItem> indicatorMap,
            Map<String, QcStandardIndicator> standardIndicatorMap) {
        return evidenceList.stream()
                .map(e -> toIndicatorDetailVo(e, indicatorMap, standardIndicatorMap))
                .collect(Collectors.toList());
    }

    private QcJudgmentResultVO.IndicatorDetailVO toIndicatorDetailVo(
            QcJudgmentEvidence e,
            Map<String, QcIndicatorItem> indicatorMap,
            Map<String, QcStandardIndicator> standardIndicatorMap) {
        QcJudgmentResultVO.IndicatorDetailVO detail = new QcJudgmentResultVO.IndicatorDetailVO();
        boolean uncoveredByStandard = !StringUtils.hasText(e.getStandardId());
        detail.setNoStandard(uncoveredByStandard);
        detail.setMeasuredValue(e.getTestValue());
        detail.setLowerLimit(e.getLowerLimit());
        detail.setUpperLimit(e.getUpperLimit());
        detail.setDeviation(e.getDeviation());
        detail.setTriggeredRule(e.getTriggerRule());
        QcIndicatorItem indicator = indicatorMap.get(e.getIndicatorId());
        if (indicator != null) {
            detail.setIndicatorName(indicator.getIndicatorName());
        }
        if (StringUtils.hasText(e.getStandardId()) && StringUtils.hasText(e.getIndicatorId())) {
            QcStandardIndicator si = standardIndicatorMap.get(
                    standardIndicatorKey(e.getStandardId(), e.getIndicatorId()));
            if (si != null) {
                detail.setConcessionLower(si.getConcessionLower());
                detail.setConcessionUpper(si.getConcessionUpper());
            }
        }
        detail.setIndicatorResult(resolveIndicatorResultCode(uncoveredByStandard, e.getIsPassed(), e.getTriggerRule()));
        return detail;
    }

    /**
     * 指标行结论：合格限内=PASS；让步范围内=CONCESSION；无标准=WARNING；否则=FAIL。
     * isPassed=0 且触发规则含「让步范围内」时不能映射为 FAIL（引擎对让步项亦记 passed=false）。
     */
    private String resolveIndicatorResultCode(boolean uncoveredByStandard, Integer passedFlag, String triggerRule) {
        if (uncoveredByStandard) {
            return JudgmentExplainConstants.INDICATOR_RESULT_WARNING;
        }
        if (triggerRule != null && triggerRule.contains(JudgmentExplainConstants.TRIGGER_RULE_CONCESSION_MARKER)) {
            return JudgmentExplainConstants.INDICATOR_RESULT_CONCESSION;
        }
        if (Integer.valueOf(JudgmentExplainConstants.PASSED_FLAG).equals(passedFlag)) {
            return JudgmentExplainConstants.INDICATOR_RESULT_PASS;
        }
        return JudgmentExplainConstants.INDICATOR_RESULT_FAIL;
    }

    private int resolvePageNum(Integer pageNum) {
        return pageNum != null && pageNum > 0 ? pageNum : 1;
    }

    private int resolvePageSize(Integer pageSize) {
        return pageSize != null && pageSize > 0 ? pageSize : 20;
    }

    private Page<QcJudgmentListVO> emptyJudgmentListPage(int pageNum, int pageSize) {
        Page<QcJudgmentListVO> empty = new Page<>(pageNum, pageSize, 0);
        empty.setRecords(Collections.emptyList());
        return empty;
    }

    private LambdaQueryWrapper<QcJudgmentResult> buildJudgmentPageWrapper(
            QcJudgmentPageQuery query, Set<String> recordIdFilter) {
        LambdaQueryWrapper<QcJudgmentResult> wrapper = new LambdaQueryWrapper<QcJudgmentResult>()
                .in(recordIdFilter != null, QcJudgmentResult::getRecordId, recordIdFilter)
                .eq(StringUtils.hasText(query.getJudgmentType()),
                        QcJudgmentResult::getJudgmentType, query.getJudgmentType())
                .eq(query.getIsFinal() != null, QcJudgmentResult::getIsFinal, query.getIsFinal())
                .orderByDesc(QcJudgmentResult::getJudgmentTime);
        if (StringUtils.hasText(query.getTimeStart())) {
            wrapper.ge(QcJudgmentResult::getJudgmentTime,
                    LocalDateTime.parse(query.getTimeStart(), FORMATTER));
        }
        if (StringUtils.hasText(query.getTimeEnd())) {
            wrapper.le(QcJudgmentResult::getJudgmentTime,
                    LocalDateTime.parse(query.getTimeEnd(), FORMATTER));
        }
        return wrapper;
    }

    private IPage<QcJudgmentListVO> convertToJudgmentListPage(IPage<QcJudgmentResult> entityPage) {
        Map<String, QcInspectionRecord> recordMap = loadInspectionRecordMap(
                entityPage.getRecords().stream()
                        .map(QcJudgmentResult::getRecordId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet()));
        Map<String, String> userNameMap = loadUserNameMap(
                recordMap.values().stream()
                        .map(QcInspectionRecord::getTesterNo)
                        .filter(StringUtils::hasText)
                        .collect(Collectors.toSet()));
        List<QcJudgmentListVO> voList = entityPage.getRecords().stream()
                .map(j -> toJudgmentListVO(j, recordMap.get(j.getRecordId()), userNameMap))
                .collect(Collectors.toList());
        Page<QcJudgmentListVO> voPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }

    private QcJudgmentListVO toJudgmentListVO(QcJudgmentResult j, QcInspectionRecord record, Map<String, String> userNameMap) {
        QcJudgmentListVO vo = new QcJudgmentListVO();
        vo.setId(j.getId());
        vo.setRecordId(j.getRecordId());
        vo.setJudgmentType(j.getJudgmentType());
        vo.setJudgmentTime(j.getJudgmentTime() != null ? j.getJudgmentTime().format(FORMATTER) : null);
        vo.setIsFinal(j.getIsFinal());
        if (record != null) {
            vo.setCoilNo(record.getCoilNo());
            vo.setBatchNo(record.getBatchNo());
            vo.setHeatNo(record.getHeatNo());
            vo.setProductVariety(record.getProductVariety());
            vo.setProductGrade(record.getProductGrade());
            vo.setSampleType(record.getSampleType());
            vo.setCustomerId(record.getCustomerId());
            vo.setTesterNo(record.getTesterNo());
            vo.setInspector(userNameMap.getOrDefault(record.getTesterNo(), record.getTesterNo()));
        }
        return vo;
    }

    private Set<String> resolveRecordIdsByCoilOrBatch(String coilNo, String batchNo) {
        if (!StringUtils.hasText(coilNo) && !StringUtils.hasText(batchNo)) {
            return null;
        }
        LambdaQueryWrapper<QcInspectionRecord> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(coilNo)) {
            wrapper.like(QcInspectionRecord::getCoilNo, coilNo);
        }
        if (StringUtils.hasText(batchNo)) {
            wrapper.like(QcInspectionRecord::getBatchNo, batchNo);
        }
        return inspectionRecordMapper.selectList(wrapper).stream()
                .map(QcInspectionRecord::getId)
                .collect(Collectors.toSet());
    }

    private Map<String, QcInspectionRecord> loadInspectionRecordMap(Set<String> recordIds) {
        if (recordIds == null || recordIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return inspectionRecordMapper.selectBatchIds(recordIds).stream()
                .collect(Collectors.toMap(QcInspectionRecord::getId, r -> r, (a, b) -> a));
    }

    private Map<String, String> loadUserNameMap(Set<String> userNos) {
        if (userNos == null || userNos.isEmpty()) {
            return Collections.emptyMap();
        }
        return sysUserMapper.selectList(new LambdaQueryWrapper<SysUser>().in(SysUser::getUserNo, userNos))
                .stream()
                .collect(Collectors.toMap(SysUser::getUserNo, SysUser::getUsername, (a, b) -> a));
    }

    /**
     * 解析matchedStandardIds JSON字符串
     */
    private List<String> parseMatchedStandardIds(String json) {
        if (!StringUtils.hasText(json)) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            log.warn("解析matchedStandardIds失败，json={}", json, e);
            return Collections.emptyList();
        }
    }
}
