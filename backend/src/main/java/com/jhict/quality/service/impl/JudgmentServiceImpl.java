package com.jhict.quality.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jhict.quality.common.constant.JudgmentExplainConstants;
import com.jhict.quality.common.entity.ApiResult;
import com.jhict.quality.common.exception.ServiceException;
import com.jhict.quality.dto.AiAssessmentCreateCmd;
import com.jhict.quality.dto.StandardConflictCreateCmd;
import com.jhict.quality.dto.StandardCandidateQuery;
import com.jhict.quality.dto.StandardConflictPageQuery;
import com.jhict.quality.enums.JudgmentType;
import com.jhict.quality.enums.StandardType;
import com.jhict.quality.dto.QcJudgmentPageQuery;
import com.jhict.quality.engine.model.JudgmentOutput;
import com.jhict.quality.entity.*;
import com.jhict.quality.gateway.model.ModelChatRequest;
import com.jhict.quality.gateway.model.ModelChatResponse;
import com.jhict.quality.gateway.model.ModelGateway;
import com.jhict.quality.gateway.model.ModelMessage;
import com.jhict.quality.mapper.*;
import com.jhict.quality.service.api.AiAssessmentService;
import com.jhict.quality.service.api.AiCacheService;
import com.jhict.quality.service.api.JudgmentService;
import com.jhict.quality.service.api.NotificationService;
import com.jhict.quality.service.api.StandardConflictService;
import com.jhict.quality.service.api.StandardService;
import com.jhict.quality.entity.SysUser;
import com.jhict.quality.mapper.SysUserMapper;
import com.jhict.quality.vo.DashboardSummaryVO;
import com.jhict.quality.vo.QcJudgmentListVO;
import com.jhict.quality.vo.QcJudgmentResultVO;
import com.jhict.quality.vo.AiSourceReferenceVO;
import com.jhict.quality.vo.StandardCandidateSetVO;
import com.jhict.quality.vo.StandardCandidateVO;
import com.jhict.quality.vo.StandardConflictVO;
import com.jhict.quality.service.support.rag.CitationReferenceLoader;
import com.jhict.quality.service.support.rag.CitationReferenceSupport;
import com.jhict.quality.service.support.rag.CitationReferenceSupport.CitationPromptStyle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class JudgmentServiceImpl implements JudgmentService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String DASHBOARD_CACHE_KEY_PREFIX = "dashboard:summary:v2:";
    private static final String DEFAULT_COMPANY_ID = "DEFAULT";
    private static final long DASHBOARD_TTL_SECONDS = 60L;
    private static final String EXPLANATION_PROMPT_VERSION = "judgment-explanation-v3";
    private static final String ASSESSMENT_TYPE_JUDGMENT_EXPLANATION = "JUDGMENT_EXPLANATION";

    private enum AiExplanationSource {
        SKIPPED,
        CACHE,
        ASSESSMENT,
        GENERATED,
        STRUCTURED
    }

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
    private StandardConflictService standardConflictService;

    @Resource
    private StandardService standardService;

    @Resource
    private CitationReferenceLoader citationReferenceLoader;

    @Resource
    private ModelGateway modelGateway;

    @Resource
    private AiAssessmentService aiAssessmentService;

    @Resource
    private AiCacheService aiCacheService;

    /** 懒注入，避免循环依赖 */
    @Lazy
    @Autowired
    private NotificationService notificationService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public QcJudgmentResult saveJudgmentResult(String recordId, JudgmentOutput output) {
        // Step 1: 保存JudgmentResult
        judgmentResultMapper.clearFinalByRecordId(recordId);
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

        // Step 4: 保存标准冲突记录。优先级可解冲突用于解释/风险提示，阻断冲突对应 STANDARD_CONFLICT。
        if (output.getConflicts() != null && !output.getConflicts().isEmpty()) {
            standardConflictService.saveDetectedConflicts(recordId, judgmentId,
                    output.getConflicts().stream().map(this::toConflictCreateCmd).collect(Collectors.toList()));
        }

        log.info("保存判定结论成功，judgmentId={}, recordId={}, type={}",
                judgmentId, recordId, output.getJudgmentType());
        return judgmentResult;
    }

    private StandardConflictCreateCmd toConflictCreateCmd(JudgmentOutput.StandardConflictItem item) {
        StandardConflictCreateCmd cmd = new StandardConflictCreateCmd();
        cmd.setConflictType(item.getConflictType());
        cmd.setConflictLevel(item.getConflictLevel());
        cmd.setStatus(item.getStatus());
        cmd.setIndicatorId(item.getIndicatorId());
        cmd.setIndicatorName(item.getIndicatorName());
        cmd.setUnit(item.getUnit());
        cmd.setCustomerId(item.getCustomerId());
        cmd.setVariety(item.getVariety());
        cmd.setGrade(item.getGrade());
        cmd.setProductSpec(item.getProductSpec());
        cmd.setInspectionDate(item.getInspectionDate());
        cmd.setSelectedStandardId(item.getSelectedStandardId());
        cmd.setInvolvedStandardIds(item.getInvolvedStandardIds());
        cmd.setConflictDetail(item.getConflictDetail());
        cmd.setSelectedPriority(item.getSelectedPriority());
        return cmd;
    }

    @Override
    public QcJudgmentResultVO getCurrentJudgment(String recordId) {
        QcJudgmentResult result = judgmentResultMapper.findFinalByRecordId(recordId);
        if (result == null) {
            throw new ServiceException(ApiResult.CODE_NOT_FOUND, "未找到该检验记录的最终判定结论");
        }

        QcInspectionRecord record = inspectionRecordMapper.selectById(recordId);
        return buildJudgmentResultVO(result, record, true, false, false);
    }

    @Override
    public QcJudgmentResultVO getExplanation(String id) {
        QcJudgmentResult result = judgmentResultMapper.selectById(id);
        if (result == null) {
            throw new ServiceException(ApiResult.CODE_NOT_FOUND, "判定结论不存在");
        }

        QcInspectionRecord record = inspectionRecordMapper.selectById(result.getRecordId());
        return buildJudgmentResultVO(result, record, true, true, true);
    }

    @Override
    public QcJudgmentResultVO getExplanationSnapshot(String id) {
        QcJudgmentResult result = judgmentResultMapper.selectById(id);
        if (result == null) {
            throw new ServiceException(ApiResult.CODE_NOT_FOUND, "判定结论不存在");
        }

        QcInspectionRecord record = inspectionRecordMapper.selectById(result.getRecordId());
        return buildJudgmentResultVO(result, record, true, false, false);
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
        summary.setAiRiskWarningCount(aiAssessmentService.countPendingRiskWarnings());
        summary.setLowConfidenceReviewCount(aiAssessmentService.countPendingLowConfidenceReviews());
        summary.setPendingStandardConflictCount(standardConflictService.countPendingBlockingConflicts());
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
    private QcJudgmentResultVO buildJudgmentResultVO(QcJudgmentResult result, QcInspectionRecord record,
                                                     boolean loadEvidences,
                                                     boolean generateAiExplanation,
                                                     boolean loadExtendedContext) {
        QcJudgmentResultVO vo = new QcJudgmentResultVO();
        vo.setJudgmentId(result.getId());
        vo.setRecordId(result.getRecordId());
        fillInspectionFieldsOnVo(vo, result, record);
        fillStandardFieldsOnVo(vo, result);
        fillEvidenceFieldsOnVo(vo, result, loadEvidences);
        fillAiExplanationFieldsOnVo(vo, result, record, loadEvidences, generateAiExplanation, loadExtendedContext);
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
        vo.setCustomerId(record.getCustomerId());
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

    private void fillAiExplanationFieldsOnVo(QcJudgmentResultVO vo, QcJudgmentResult result,
                                             QcInspectionRecord record, boolean loadEvidences,
                                             boolean generateAiExplanation, boolean loadExtendedContext) {
        List<QcJudgmentEvidence> evidenceList = loadEvidences
                ? judgmentEvidenceMapper.findByJudgmentId(result.getId())
                : Collections.emptyList();
        if (loadExtendedContext) {
            fillCandidateStandardFields(vo, record, evidenceList);
        } else {
            vo.setCandidateStandards(Collections.emptyList());
            vo.setSuppressedStandards(Collections.emptyList());
        }
        List<StandardConflictVO> conflicts = loadConflictsByJudgmentId(result.getId());
        vo.setConflicts(conflicts);
        vo.setConflictWarnings(buildConflictWarnings(result, conflicts));
        List<AiSourceReferenceVO> citations = loadCitationReferences(result, vo.getEvidences());
        vo.setCitations(citations);
        vo.setCitationMissing(citations.isEmpty());
        vo.setRuleExplanation(CitationReferenceSupport.buildJudgmentRuleExplanation(
                result.getJudgmentType(),
                vo.getEvidences(),
                citations,
                conflicts == null ? 0 : conflicts.size()));
        AiExplanationSource aiSource = generateAiExplanation
                ? fillGeneratedExplanationIfPossible(vo, result, citations)
                : tryStructuredExplanation(vo, citations);
        applyConfidenceAndDegradation(vo, result, conflicts, aiSource);
        if (aiSource == AiExplanationSource.GENERATED || aiSource == AiExplanationSource.STRUCTURED) {
            persistJudgmentExplanationAssessment(vo, result);
        }
    }

    private void fillCandidateStandardFields(QcJudgmentResultVO vo, QcInspectionRecord record,
                                             List<QcJudgmentEvidence> evidenceList) {
        if (record == null) {
            vo.setCandidateStandards(Collections.emptyList());
            vo.setSuppressedStandards(Collections.emptyList());
            return;
        }
        StandardCandidateQuery query = new StandardCandidateQuery();
        query.setCustomerId(record.getCustomerId());
        query.setVariety(record.getProductVariety());
        query.setGrade(record.getProductGrade());
        query.setProductSpec(record.getProductSpec());
        query.setTestDate(record.getTestTime() == null ? null : record.getTestTime().toLocalDate());
        query.setIndicatorIds(evidenceList.stream()
                .map(QcJudgmentEvidence::getIndicatorId)
                .filter(StringUtils::hasText)
                .distinct()
                .collect(Collectors.toList()));
        try {
            StandardCandidateSetVO candidateSet = standardService.findCandidateStandards(query);
            vo.setSelectedStandard(candidateSet.getSelectedStandard());
            vo.setCandidateStandards(candidateSet.getCandidateStandards());
            vo.setSuppressedStandards(candidateSet.getSuppressedStandards());
        } catch (Exception e) {
            log.warn("填充候选标准失败，judgmentId={}, recordId={}, error={}",
                    vo.getJudgmentId(), record.getId(), e.getMessage());
            vo.setCandidateStandards(Collections.emptyList());
            vo.setSuppressedStandards(Collections.emptyList());
        }
    }

    private List<StandardConflictVO> loadConflictsByJudgmentId(String judgmentId) {
        StandardConflictPageQuery query = new StandardConflictPageQuery();
        query.setJudgmentId(judgmentId);
        query.setPageNum(1);
        query.setPageSize(100);
        return standardConflictService.page(query).getRecords();
    }

    private List<String> buildConflictWarnings(QcJudgmentResult result, List<StandardConflictVO> conflicts) {
        List<String> warnings = new ArrayList<>();
        if (JudgmentType.STANDARD_CONFLICT.getCode().equals(result.getJudgmentType())) {
            warnings.add("当前判定为标准冲突，必须完成标准冲突裁决后才能形成放行类结论");
        }
        for (StandardConflictVO conflict : conflicts) {
            warnings.add(String.format("指标[%s]存在%s冲突，级别=%s，状态=%s",
                    StringUtils.hasText(conflict.getIndicatorName()) ? conflict.getIndicatorName() : conflict.getIndicatorId(),
                    conflict.getConflictType(), conflict.getConflictLevel(), conflict.getStatus()));
        }
        return warnings;
    }

    private void applyConfidenceAndDegradation(QcJudgmentResultVO vo, QcJudgmentResult result,
                                               List<StandardConflictVO> conflicts,
                                               AiExplanationSource aiSource) {
        List<String> factors = new ArrayList<>();
        factors.add("已使用结构化判定依据生成规则解释");
        if (Boolean.TRUE.equals(vo.getCitationMissing())) {
            factors.add("来源段落引用尚未命中，禁止补写不存在的标准原文");
        } else {
            factors.add("已命中来源段落引用");
        }
        boolean hasBlockingConflict = conflicts != null && conflicts.stream()
                .anyMatch(conflict -> "BLOCKING".equals(conflict.getConflictLevel())
                        && "PENDING".equals(conflict.getStatus()));
        boolean standardGap = parseMatchedStandardIds(result.getMatchedStandardIds()).isEmpty();
        if (JudgmentType.STANDARD_CONFLICT.getCode().equals(result.getJudgmentType()) || hasBlockingConflict || standardGap) {
            vo.setConfidenceLabel("LOW");
            vo.setConfidenceScore(0.2D);
            if (standardGap) {
                factors.add("未命中结构化标准，存在标准覆盖缺口");
            }
            if (JudgmentType.STANDARD_CONFLICT.getCode().equals(result.getJudgmentType()) || hasBlockingConflict) {
                factors.add("存在未裁决标准冲突");
            }
        } else if (!Boolean.TRUE.equals(vo.getCitationMissing()) && StringUtils.hasText(vo.getAiExplanation())) {
            vo.setConfidenceLabel("HIGH");
            vo.setConfidenceScore(0.85D);
            if (aiSource == AiExplanationSource.STRUCTURED) {
                factors.add("结构化依据解释已附来源编号并通过校验");
            } else {
                factors.add("AI解释已通过来源编号校验");
            }
        } else if (!Boolean.TRUE.equals(vo.getCitationMissing())) {
            vo.setConfidenceLabel("MEDIUM");
            vo.setConfidenceScore(0.7D);
            factors.add("来源段落已命中，但未生成可信AI解释");
        } else {
            vo.setConfidenceLabel("MEDIUM");
            vo.setConfidenceScore(0.6D);
            factors.add("结构化规则完整，但来源段落引用缺失");
        }
        vo.setConfidenceFactors(factors);
        if (StringUtils.hasText(vo.getAiExplanation())) {
            if (aiSource == AiExplanationSource.STRUCTURED) {
                vo.setDegradationSource("RULE_TEMPLATE");
                vo.setDegradationReason("模型解释未通过校验，已展示含指标依据与来源编号的结构化解释");
            } else {
                vo.setDegradationSource("GENERATED");
                vo.setDegradationReason("AI解释已基于来源条款生成并通过引用校验");
            }
        } else if (!Boolean.TRUE.equals(vo.getCitationMissing())) {
            vo.setDegradationSource("RAW_RETRIEVAL");
            vo.setDegradationReason("已返回来源条款和规则模板，AI解释不可用或引用校验未通过");
        } else {
            vo.setDegradationSource("RULE_TEMPLATE");
            vo.setDegradationReason("当前返回规则模板解释，来源段落尚未命中");
        }
    }

    private List<AiSourceReferenceVO> loadCitationReferences(QcJudgmentResult result,
                                                            List<QcJudgmentResultVO.EvidenceVO> evidences) {
        return citationReferenceLoader.loadForJudgment(parseMatchedStandardIds(result.getMatchedStandardIds()), evidences);
    }

    private AiExplanationSource fillGeneratedExplanationIfPossible(QcJudgmentResultVO vo,
                                                                   QcJudgmentResult result,
                                                                   List<AiSourceReferenceVO> citations) {
        vo.setAiExplanation(null);
        if (citations == null || citations.isEmpty()) {
            return tryStructuredExplanation(vo, citations);
        }
        if (!modelGateway.enabled()) {
            return tryStructuredExplanation(vo, citations);
        }
        String cachedExplanation = loadCachedExplanation(result.getId(), citations, vo.getEvidences());
        if (StringUtils.hasText(cachedExplanation)) {
            vo.setAiExplanation(cachedExplanation);
            return AiExplanationSource.CACHE;
        }
        String assessedExplanation = loadAssessedExplanation(result.getId(), citations, vo.getEvidences());
        if (StringUtils.hasText(assessedExplanation)) {
            vo.setAiExplanation(assessedExplanation);
            return AiExplanationSource.ASSESSMENT;
        }
        ModelChatResponse response = modelGateway.chat(buildExplanationChatRequest(vo, result, citations));
        String trusted = CitationReferenceSupport.acceptTrustedCitedOutput(
                response != null ? response.getContent() : null, citations, vo.getEvidences());
        if (StringUtils.hasText(trusted)) {
            vo.setAiExplanation(trusted);
            return AiExplanationSource.GENERATED;
        }
        if (response != null && response.isSuccess()) {
            log.warn("AI判定解释引用校验未通过，judgmentId={}", result.getId());
        } else if (response != null) {
            log.warn("AI判定解释生成失败，judgmentId={}, error={}", result.getId(), response.getErrorMessage());
        }
        return tryStructuredExplanation(vo, citations);
    }

    private AiExplanationSource tryStructuredExplanation(QcJudgmentResultVO vo,
                                                         List<AiSourceReferenceVO> citations) {
        if (citations == null || citations.isEmpty()) {
            return AiExplanationSource.SKIPPED;
        }
        String structured = CitationReferenceSupport.acceptTrustedCitedOutput(
                vo.getRuleExplanation(), citations, vo.getEvidences());
        if (StringUtils.hasText(structured)) {
            vo.setAiExplanation(structured);
            return AiExplanationSource.STRUCTURED;
        }
        return AiExplanationSource.SKIPPED;
    }

    private String loadCachedExplanation(String judgmentId,
                                         List<AiSourceReferenceVO> citations,
                                         List<QcJudgmentResultVO.EvidenceVO> evidences) {
        QcAiCache cache = aiCacheService.findActive(ASSESSMENT_TYPE_JUDGMENT_EXPLANATION, "JUDGMENT", judgmentId);
        if (cache == null || !StringUtils.hasText(cache.getCachedOutput())) {
            return null;
        }
        String cachedText = parseCachedExplanationText(cache.getCachedOutput());
        String trusted = CitationReferenceSupport.acceptTrustedCitedOutput(cachedText, citations, evidences);
        if (!StringUtils.hasText(trusted)) {
            log.warn("判定解释缓存引用校验未通过，judgmentId={}", judgmentId);
            return null;
        }
        return trusted;
    }

    private String loadAssessedExplanation(String judgmentId,
                                           List<AiSourceReferenceVO> citations,
                                           List<QcJudgmentResultVO.EvidenceVO> evidences) {
        return aiAssessmentService.findLatestByRelatedJudgment(judgmentId, ASSESSMENT_TYPE_JUDGMENT_EXPLANATION)
                .filter(assessment -> EXPLANATION_PROMPT_VERSION.equals(assessment.getPromptVersion()))
                .map(QcAiAssessment::getRawOutput)
                .map(content -> CitationReferenceSupport.acceptTrustedCitedOutput(content, citations, evidences))
                .filter(StringUtils::hasText)
                .orElse(null);
    }

    private String parseCachedExplanationText(String cachedOutput) {
        if (!StringUtils.hasText(cachedOutput)) {
            return null;
        }
        try {
            Map<String, Object> map = objectMapper.readValue(cachedOutput, new TypeReference<Map<String, Object>>() {
            });
            Object summary = map.get("summary");
            if (summary != null) {
                return summary.toString();
            }
            Object answer = map.get("answer");
            if (answer != null) {
                return answer.toString();
            }
        } catch (Exception e) {
            log.debug("判定解释缓存非 JSON，按纯文本使用，error={}", e.getMessage());
        }
        return cachedOutput;
    }

    private ModelChatRequest buildExplanationChatRequest(QcJudgmentResultVO vo,
                                                        QcJudgmentResult result,
                                                        List<AiSourceReferenceVO> citations) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("判定结论：").append(result.getJudgmentType()).append("\n");
        prompt.append("规则解释：").append(vo.getRuleExplanation()).append("\n\n");
        prompt.append("指标依据：\n");
        if (vo.getEvidences() != null) {
            for (QcJudgmentResultVO.EvidenceVO evidence : vo.getEvidences()) {
                prompt.append("- ")
                        .append(nullToEmpty(evidence.getIndicatorName()))
                        .append(" 实测=").append(evidence.getTestValue())
                        .append(" 下限=").append(evidence.getLowerLimit())
                        .append(" 上限=").append(evidence.getUpperLimit())
                        .append(" 偏差=").append(evidence.getDeviation())
                        .append(" 规则=").append(nullToEmpty(evidence.getTriggerRule()))
                        .append("\n");
            }
        }
        CitationReferenceSupport.appendNumberedCitationBlock(prompt, citations);
        CitationReferenceSupport.appendCitationAnswerRules(prompt, CitationPromptStyle.JUDGMENT_EXPLANATION);
        return ModelChatRequest.builder()
                .businessType("JUDGMENT_EXPLANATION")
                .businessId(result.getId())
                .promptVersion(EXPLANATION_PROMPT_VERSION)
                .systemPrompt("你是钢铁质量判定解释助手。不得使用来源条款之外的标准、限值或案例；缺少依据时必须说明引用缺失。")
                .messages(Collections.singletonList(ModelMessage.builder()
                        .role("user")
                        .content(prompt.toString())
                        .build()))
                .temperature(0.1D)
                .maxTokens(600)
                .build();
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private void persistJudgmentExplanationAssessment(QcJudgmentResultVO vo, QcJudgmentResult result) {
        try {
            AiAssessmentCreateCmd cmd = new AiAssessmentCreateCmd();
            cmd.setAssessmentType("JUDGMENT_EXPLANATION");
            cmd.setBusinessType("QC_JUDGMENT_RESULT");
            cmd.setBusinessId(result.getId());
            cmd.setRelatedJudgmentId(result.getId());
            cmd.setInputSnapshot(buildExplanationInputSnapshot(vo));
            cmd.setReferencesJson(objectMapper.writeValueAsString(vo.getCitations()));
            cmd.setModelProvider(StringUtils.hasText(vo.getAiExplanation()) ? modelGateway.provider() : null);
            cmd.setPromptVersion(EXPLANATION_PROMPT_VERSION);
            cmd.setRawOutput(StringUtils.hasText(vo.getAiExplanation()) ? vo.getAiExplanation() : vo.getRuleExplanation());
            cmd.setStructuredOutput(buildExplanationStructuredOutput(vo));
            cmd.setConfidenceLabel(vo.getConfidenceLabel());
            cmd.setConfidenceScore(vo.getConfidenceScore() == null ? null : BigDecimal.valueOf(vo.getConfidenceScore()));
            cmd.setConfidenceFactors(objectMapper.writeValueAsString(vo.getConfidenceFactors()));
            cmd.setDegradationSource(vo.getDegradationSource());
            cmd.setCacheHit(0);
            aiAssessmentService.create(cmd);
        } catch (Exception e) {
            log.warn("保存AI判定解释审计失败，judgmentId={}, error={}", result.getId(), e.getMessage());
        }
    }

    private String buildExplanationInputSnapshot(QcJudgmentResultVO vo) throws Exception {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("judgmentId", vo.getJudgmentId());
        snapshot.put("recordId", vo.getRecordId());
        snapshot.put("judgmentType", vo.getJudgmentType());
        snapshot.put("coilNo", vo.getCoilNo());
        snapshot.put("batchNo", vo.getBatchNo());
        snapshot.put("productVariety", vo.getProductVariety());
        snapshot.put("productGrade", vo.getProductGrade());
        snapshot.put("productSpec", vo.getProductSpec());
        snapshot.put("matchedStandardIds", vo.getMatchedStandards() == null ? Collections.emptyList()
                : vo.getMatchedStandards().stream()
                .map(QcJudgmentResultVO.MatchedStandardVO::getStandardId)
                .collect(Collectors.toList()));
        snapshot.put("candidateStandardIds", vo.getCandidateStandards() == null ? Collections.emptyList()
                : vo.getCandidateStandards().stream()
                .map(StandardCandidateVO::getId)
                .collect(Collectors.toList()));
        snapshot.put("evidenceCount", vo.getEvidences() == null ? 0 : vo.getEvidences().size());
        snapshot.put("conflictCount", vo.getConflicts() == null ? 0 : vo.getConflicts().size());
        return objectMapper.writeValueAsString(snapshot);
    }

    private String buildExplanationStructuredOutput(QcJudgmentResultVO vo) throws Exception {
        Map<String, Object> output = new LinkedHashMap<>();
        output.put("ruleExplanation", vo.getRuleExplanation());
        output.put("aiExplanation", vo.getAiExplanation());
        output.put("confidenceLabel", vo.getConfidenceLabel());
        output.put("confidenceScore", vo.getConfidenceScore());
        output.put("degradationSource", vo.getDegradationSource());
        output.put("degradationReason", vo.getDegradationReason());
        output.put("citationMissing", vo.getCitationMissing());
        output.put("conflictWarnings", vo.getConflictWarnings());
        return objectMapper.writeValueAsString(output);
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
        if (JudgmentExplainConstants.isConcessionTriggerRule(triggerRule)) {
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
