package com.jhict.quality.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jhict.quality.common.entity.ApiResult;
import com.jhict.quality.common.exception.ServiceException;
import com.jhict.quality.dto.QcJudgmentPageQuery;
import com.jhict.quality.engine.model.JudgmentOutput;
import com.jhict.quality.entity.*;
import com.jhict.quality.mapper.*;
import com.jhict.quality.service.api.JudgmentService;
import com.jhict.quality.service.api.NotificationService;
import com.jhict.quality.vo.DashboardSummaryVO;
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
    private QcInspectionRecordMapper inspectionRecordMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

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

        log.info("保存判定结论成功，judgmentId={}, recordId={}, type={}",
                judgmentId, recordId, output.getJudgmentType());
        return judgmentResult;
    }

    @Override
    public QcJudgmentResultVO getCurrentJudgment(String recordId) {
        QcJudgmentResult result = judgmentResultMapper.findFinalByRecordId(recordId);
        if (result == null) {
            throw new ServiceException(ApiResult.CODE_NOT_FOUND, "未找到该检验记录的最终判定结论");
        }

        // 查询检验记录获取卷号
        QcInspectionRecord record = inspectionRecordMapper.selectById(recordId);
        String coilNo = record != null ? record.getCoilNo() : null;

        return buildJudgmentResultVO(result, coilNo, true);
    }

    @Override
    public QcJudgmentResultVO getExplanation(String id) {
        QcJudgmentResult result = judgmentResultMapper.selectById(id);
        if (result == null) {
            throw new ServiceException(ApiResult.CODE_NOT_FOUND, "判定结论不存在");
        }

        // 查询检验记录获取卷号
        QcInspectionRecord record = inspectionRecordMapper.selectById(result.getRecordId());
        String coilNo = record != null ? record.getCoilNo() : null;

        return buildJudgmentResultVO(result, coilNo, true);
    }

    @Override
    public IPage<QcJudgmentResult> page(QcJudgmentPageQuery query) {
        Page<QcJudgmentResult> pageParam = new Page<>(query.getPageNum(), query.getPageSize());

        LambdaQueryWrapper<QcJudgmentResult> wrapper = new LambdaQueryWrapper<QcJudgmentResult>()
                .eq(StringUtils.hasText(query.getJudgmentType()),
                        QcJudgmentResult::getJudgmentType, query.getJudgmentType())
                .eq(query.getIsFinal() != null, QcJudgmentResult::getIsFinal, query.getIsFinal())
                .ge(StringUtils.hasText(query.getTimeStart()),
                        QcJudgmentResult::getJudgmentTime,
                        StringUtils.hasText(query.getTimeStart())
                                ? LocalDateTime.parse(query.getTimeStart(), FORMATTER) : null)
                .le(StringUtils.hasText(query.getTimeEnd()),
                        QcJudgmentResult::getJudgmentTime,
                        StringUtils.hasText(query.getTimeEnd())
                                ? LocalDateTime.parse(query.getTimeEnd(), FORMATTER) : null)
                .orderByDesc(QcJudgmentResult::getJudgmentTime);

        return judgmentResultMapper.selectPage(pageParam, wrapper);
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
     * 构建JudgmentResultVO（含evidences和matchedStandards）
     */
    private QcJudgmentResultVO buildJudgmentResultVO(QcJudgmentResult result, String coilNo, boolean loadEvidences) {
        QcJudgmentResultVO vo = new QcJudgmentResultVO();
        vo.setJudgmentId(result.getId());
        vo.setRecordId(result.getRecordId());
        vo.setCoilNo(coilNo);
        vo.setJudgmentType(result.getJudgmentType());
        vo.setJudgmentTime(result.getJudgmentTime() != null
                ? result.getJudgmentTime().format(FORMATTER) : null);
        vo.setIsFinal(result.getIsFinal());

        // 解析matchedStandardIds
        List<String> standardIds = parseMatchedStandardIds(result.getMatchedStandardIds());
        List<QcJudgmentResultVO.MatchedStandardVO> matchedStandards = new ArrayList<>();
        if (!standardIds.isEmpty()) {
            List<QcQualityStandard> standards = qualityStandardMapper.selectBatchIds(standardIds);
            matchedStandards = standards.stream().map(s -> {
                QcJudgmentResultVO.MatchedStandardVO msVO = new QcJudgmentResultVO.MatchedStandardVO();
                msVO.setStandardId(s.getId());
                msVO.setStandardType(s.getStandardType());
                msVO.setVersionNo(s.getVersionNo());
                return msVO;
            }).collect(Collectors.toList());
        }
        vo.setMatchedStandards(matchedStandards);

        // 加载evidences
        if (loadEvidences) {
            List<QcJudgmentEvidence> evidenceList = judgmentEvidenceMapper.findByJudgmentId(result.getId());

            // 批量查询指标元信息
            List<String> indicatorIds = evidenceList.stream()
                    .map(QcJudgmentEvidence::getIndicatorId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());
            Map<String, QcIndicatorItem> indicatorMap = new HashMap<>();
            if (!indicatorIds.isEmpty()) {
                indicatorItemMapper.selectBatchIds(indicatorIds)
                        .forEach(item -> indicatorMap.put(item.getId(), item));
            }

            List<QcJudgmentResultVO.EvidenceVO> evidenceVOList = evidenceList.stream().map(e -> {
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
            vo.setEvidences(evidenceVOList);
        } else {
            vo.setEvidences(Collections.emptyList());
        }

        return vo;
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
