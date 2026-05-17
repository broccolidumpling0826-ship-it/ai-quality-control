package com.jhict.quality.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jhict.quality.common.constant.JudgmentExplainConstants;
import com.jhict.quality.common.exception.ServiceException;
import com.jhict.quality.dto.QcQualityCertGenerateCmd;
import com.jhict.quality.entity.*;
import com.jhict.quality.mapper.*;
import com.jhict.quality.service.api.CertDataService;
import com.jhict.quality.vo.QcQualityCertDataVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.HashSet;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CertDataServiceImpl implements CertDataService {

    @Resource
    private QcQualityCertDataMapper certDataMapper;

    @Resource
    private QcInspectionRecordMapper inspectionRecordMapper;

    @Resource
    private QcInspectionValueMapper inspectionValueMapper;

    @Resource
    private QcJudgmentResultMapper judgmentResultMapper;

    @Resource
    private QcIndicatorItemMapper indicatorItemMapper;

    @Resource
    private QcJudgmentEvidenceMapper judgmentEvidenceMapper;

    @Resource
    private QcConcessionAcceptanceMapper concessionMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /** 质保书纳入的指标类别（D-017），默认成分/性能/尺寸；ADMIN 可通过 application.yml 调整 */
    @Value("${app.cert.included-categories:COMPOSITION,PERFORMANCE,DIMENSION}")
    private String includedCategoriesConfig;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public QcQualityCertDataVO generate(QcQualityCertGenerateCmd cmd) {
        // 1. 按 coilNo 或 batchNo 查 NORMAL 状态记录
        LambdaQueryWrapper<QcInspectionRecord> recordWrapper = new LambdaQueryWrapper<QcInspectionRecord>()
                .eq(QcInspectionRecord::getStatus, "NORMAL");
        if ("COIL".equals(cmd.getQueryType())) {
            if (!StringUtils.hasText(cmd.getCoilNo())) {
                throw new ServiceException("查询类型为 COIL 时，coilNo 不能为空");
            }
            recordWrapper.eq(QcInspectionRecord::getCoilNo, cmd.getCoilNo());
        } else {
            if (!StringUtils.hasText(cmd.getBatchNo())) {
                throw new ServiceException("查询类型为 BATCH 时，batchNo 不能为空");
            }
            recordWrapper.eq(QcInspectionRecord::getBatchNo, cmd.getBatchNo());
        }

        List<QcInspectionRecord> records = inspectionRecordMapper.selectList(recordWrapper);
        if (records.isEmpty()) {
            throw new ServiceException("未找到对应的有效检验记录");
        }
        records = pickLatestRecordsForCert(records, cmd.getQueryType());

        // 2. 收集记录ID，查询最终判定和检验值
        List<String> recordIds = records.stream().map(QcInspectionRecord::getId).collect(Collectors.toList());

        // 查最终判定结论（isFinal=1）
        Map<String, QcJudgmentResult> finalJudgmentMap = judgmentResultMapper.selectList(
                new LambdaQueryWrapper<QcJudgmentResult>()
                        .in(QcJudgmentResult::getRecordId, recordIds)
                        .eq(QcJudgmentResult::getIsFinal, 1)
        ).stream().collect(Collectors.toMap(
                QcJudgmentResult::getRecordId,
                j -> j,
                (a, b) -> compareJudgmentTime(a, b) >= 0 ? a : b));

        // 3. 组装 indicator 快照
        List<QcQualityCertDataVO.IndicatorSnapshot> snapshots = new ArrayList<>();

        for (QcInspectionRecord record : records) {
            QcJudgmentResult judgment = finalJudgmentMap.get(record.getId());
            String finalJudgmentType = judgment != null ? judgment.getJudgmentType() : null;
            boolean concessionApproved = judgment != null && hasApprovedConcession(judgment.getId());

            // 查检验值
            List<QcInspectionValue> values = inspectionValueMapper.selectList(
                    new LambdaQueryWrapper<QcInspectionValue>()
                            .eq(QcInspectionValue::getRecordId, record.getId())
            );

            List<QcJudgmentEvidence> evidences = resolveEvidencesForCert(judgment, record.getId());

            Map<String, QcJudgmentEvidence> evidenceByIndicator = evidences.stream()
                    .collect(Collectors.toMap(QcJudgmentEvidence::getIndicatorId, e -> e, (a, b) -> a));

            // D-017: 仅纳入配置的指标类别（默认 COMPOSITION/PERFORMANCE/DIMENSION）
            Set<String> includedCategories = new HashSet<>(
                    Arrays.asList(includedCategoriesConfig.split(",")));

            for (QcInspectionValue value : values) {
                QcIndicatorItem indicator = indicatorItemMapper.selectById(value.getIndicatorId());

                // 过滤：不在纳入类别内的指标跳过
                if (indicator != null && StringUtils.hasText(indicator.getIndicatorCategory())
                        && !includedCategories.contains(indicator.getIndicatorCategory())) {
                    continue;
                }

                QcJudgmentEvidence evidence = evidenceByIndicator.get(value.getIndicatorId());

                QcQualityCertDataVO.IndicatorSnapshot snap = new QcQualityCertDataVO.IndicatorSnapshot();
                snap.setIndicatorName(indicator != null ? indicator.getIndicatorName() : value.getIndicatorId());
                snap.setIndicatorCode(indicator != null ? indicator.getIndicatorCode() : null);
                snap.setUnit(indicator != null ? indicator.getUnit() : null);
                snap.setTestValue(value.getTestValue());
                snap.setUpperLimit(evidence != null ? evidence.getUpperLimit() : null);
                snap.setLowerLimit(evidence != null ? evidence.getLowerLimit() : null);
                String indicatorResult = resolveCertIndicatorResult(evidence, finalJudgmentType, concessionApproved);
                snap.setIndicatorResult(indicatorResult);
                snap.setIsPassed(toCertIsPassed(indicatorResult));
                snap.setFinalJudgmentType(finalJudgmentType);
                snapshots.add(snap);
            }
        }

        // 4. 序列化 snapshot，保存
        String snapshotJson;
        try {
            snapshotJson = objectMapper.writeValueAsString(snapshots);
        } catch (Exception e) {
            throw new ServiceException("快照序列化失败：" + e.getMessage());
        }

        String generatedBy = getLoginUserNo();
        QcInspectionRecord primary = records.get(0);
        QcQualityCertData certData = new QcQualityCertData();
        certData.setCoilNo(StringUtils.hasText(cmd.getCoilNo()) ? cmd.getCoilNo() : primary.getCoilNo());
        certData.setBatchNo(StringUtils.hasText(cmd.getBatchNo()) ? cmd.getBatchNo() : primary.getBatchNo());
        certData.setSnapshotData(snapshotJson);
        certData.setGenerateTime(LocalDateTime.now());
        certData.setGeneratedBy(generatedBy);
        certDataMapper.insert(certData);

        log.info("质保书数据生成成功，id={}，generatedBy={}", certData.getId(), generatedBy);
        return toVO(certData);
    }

    @Override
    public QcQualityCertDataVO getById(String id) {
        QcQualityCertData data = certDataMapper.selectById(id);
        if (data == null) {
            throw new ServiceException("质保书数据不存在");
        }
        return toVO(data);
    }

    @Override
    public IPage<QcQualityCertDataVO> page(int pageNum, int pageSize, String coilNo, String batchNo) {
        LambdaQueryWrapper<QcQualityCertData> wrapper = new LambdaQueryWrapper<QcQualityCertData>()
                .orderByDesc(QcQualityCertData::getGenerateTime);
        if (StringUtils.hasText(coilNo)) {
            wrapper.eq(QcQualityCertData::getCoilNo, coilNo);
        }
        if (StringUtils.hasText(batchNo)) {
            wrapper.eq(QcQualityCertData::getBatchNo, batchNo);
        }

        IPage<QcQualityCertData> pageResult = certDataMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);

        List<QcQualityCertDataVO> voList = pageResult.getRecords().stream()
                .map(this::toVO)
                .collect(Collectors.toList());

        Page<QcQualityCertDataVO> voPage = new Page<>(pageResult.getCurrent(), pageResult.getSize(), pageResult.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }

    private QcQualityCertDataVO toVO(QcQualityCertData data) {
        QcQualityCertDataVO vo = new QcQualityCertDataVO();
        vo.setId(data.getId());
        vo.setCoilNo(data.getCoilNo());
        vo.setBatchNo(data.getBatchNo());
        vo.setGenerateTime(data.getGenerateTime());
        vo.setGeneratedBy(data.getGeneratedBy());

        if (StringUtils.hasText(data.getSnapshotData())) {
            try {
                List<QcQualityCertDataVO.IndicatorSnapshot> snapshots = objectMapper.readValue(
                        data.getSnapshotData(),
                        objectMapper.getTypeFactory().constructCollectionType(
                                List.class, QcQualityCertDataVO.IndicatorSnapshot.class)
                );
                vo.setIndicators(snapshots);
            } catch (Exception e) {
                log.warn("质保书快照反序列化失败，id={}", data.getId(), e);
                vo.setIndicators(Collections.emptyList());
            }
        }
        enrichVoFromInspection(vo, data);
        return vo;
    }

    /** 从关联检验记录补全炉号/品种/牌号/客户，并设置展示状态 */
    private void enrichVoFromInspection(QcQualityCertDataVO vo, QcQualityCertData data) {
        QcInspectionRecord record = resolvePrimaryInspectionRecordForCert(data.getCoilNo(), data.getBatchNo());
        if (record == null) {
            vo.setStatus(vo.getIndicators() != null && !vo.getIndicators().isEmpty() ? "SUCCESS" : "FAILED");
            return;
        }
        if (record != null) {
            vo.setHeatNo(record.getHeatNo());
            vo.setProductVariety(record.getProductVariety());
            vo.setProductGrade(record.getProductGrade());
            vo.setCustomerId(record.getCustomerId());
            if (!StringUtils.hasText(vo.getCoilNo())) {
                vo.setCoilNo(record.getCoilNo());
            }
            if (!StringUtils.hasText(vo.getBatchNo())) {
                vo.setBatchNo(record.getBatchNo());
            }
            applyCurrentFinalJudgmentToCert(vo, record);
        }

        List<QcQualityCertDataVO.IndicatorSnapshot> indicators = vo.getIndicators();
        if (indicators != null && !indicators.isEmpty()) {
            vo.setStatus("SUCCESS");
            if (!StringUtils.hasText(vo.getFinalJudgmentType())) {
                indicators.stream()
                        .map(QcQualityCertDataVO.IndicatorSnapshot::getFinalJudgmentType)
                        .filter(StringUtils::hasText)
                        .findFirst()
                        .ifPresent(vo::setFinalJudgmentType);
            }
        } else {
            vo.setStatus("FAILED");
        }
    }

    /**
     * 按卷号分组，每组取「最终判定时间最新」的检验记录（改判后判定时间更新，优先于复检新建记录）。
     */
    private List<QcInspectionRecord> pickLatestRecordsForCert(List<QcInspectionRecord> records, String queryType) {
        if (records.isEmpty()) {
            return records;
        }
        Map<String, List<QcInspectionRecord>> byCoil = records.stream()
                .collect(Collectors.groupingBy(
                        r -> StringUtils.hasText(r.getCoilNo()) ? r.getCoilNo() : r.getId(),
                        LinkedHashMap::new,
                        Collectors.toList()));
        List<QcInspectionRecord> picked = new ArrayList<>();
        for (List<QcInspectionRecord> group : byCoil.values()) {
            picked.add(resolveBestRecordByFinalJudgment(group));
        }
        return picked;
    }

    /**
     * 质保书详情/补全：同一卷号多条检验记录时，取最终判定时间最新的一条（覆盖改判场景）。
     */
    private QcInspectionRecord resolvePrimaryInspectionRecordForCert(String coilNo, String batchNo) {
        if (!StringUtils.hasText(coilNo) && !StringUtils.hasText(batchNo)) {
            return null;
        }
        LambdaQueryWrapper<QcInspectionRecord> wrapper = new LambdaQueryWrapper<QcInspectionRecord>()
                .eq(QcInspectionRecord::getStatus, "NORMAL");
        if (StringUtils.hasText(coilNo)) {
            wrapper.eq(QcInspectionRecord::getCoilNo, coilNo);
        } else {
            wrapper.eq(QcInspectionRecord::getBatchNo, batchNo);
        }
        List<QcInspectionRecord> records = inspectionRecordMapper.selectList(wrapper);
        if (records.isEmpty()) {
            return null;
        }
        if (records.size() == 1) {
            return records.get(0);
        }
        return resolveBestRecordByFinalJudgment(records);
    }

    private QcInspectionRecord resolveBestRecordByFinalJudgment(List<QcInspectionRecord> records) {
        QcInspectionRecord bestRecord = null;
        LocalDateTime bestJudgmentTime = null;
        for (QcInspectionRecord record : records) {
            QcJudgmentResult finalJudgment = findFinalJudgment(record.getId());
            if (finalJudgment == null || finalJudgment.getJudgmentTime() == null) {
                continue;
            }
            if (bestJudgmentTime == null || finalJudgment.getJudgmentTime().isAfter(bestJudgmentTime)) {
                bestJudgmentTime = finalJudgment.getJudgmentTime();
                bestRecord = record;
            }
        }
        if (bestRecord != null) {
            return bestRecord;
        }
        return records.stream()
                .max(Comparator.comparing(QcInspectionRecord::getTestTime, Comparator.nullsLast(Comparator.naturalOrder())))
                .orElse(records.get(0));
    }

    private QcJudgmentResult findFinalJudgment(String recordId) {
        return judgmentResultMapper.selectOne(
                new LambdaQueryWrapper<QcJudgmentResult>()
                        .eq(QcJudgmentResult::getRecordId, recordId)
                        .eq(QcJudgmentResult::getIsFinal, 1)
                        .orderByDesc(QcJudgmentResult::getJudgmentTime)
                        .last("LIMIT 1"));
    }

    private int compareJudgmentTime(QcJudgmentResult a, QcJudgmentResult b) {
        if (a.getJudgmentTime() == null && b.getJudgmentTime() == null) {
            return 0;
        }
        if (a.getJudgmentTime() == null) {
            return -1;
        }
        if (b.getJudgmentTime() == null) {
            return 1;
        }
        return a.getJudgmentTime().compareTo(b.getJudgmentTime());
    }

    /** 解析判定依据：优先当前最终判定；改判后新判定无依据时回退到同记录历史判定 */
    private List<QcJudgmentEvidence> resolveEvidencesForCert(QcJudgmentResult judgment, String recordId) {
        if (judgment != null) {
            List<QcJudgmentEvidence> current = judgmentEvidenceMapper.selectList(
                    new LambdaQueryWrapper<QcJudgmentEvidence>()
                            .eq(QcJudgmentEvidence::getJudgmentId, judgment.getId()));
            if (!current.isEmpty()) {
                return current;
            }
        }
        List<QcJudgmentResult> history = judgmentResultMapper.selectList(
                new LambdaQueryWrapper<QcJudgmentResult>()
                        .eq(QcJudgmentResult::getRecordId, recordId)
                        .orderByDesc(QcJudgmentResult::getJudgmentTime));
        for (QcJudgmentResult j : history) {
            List<QcJudgmentEvidence> fallback = judgmentEvidenceMapper.selectList(
                    new LambdaQueryWrapper<QcJudgmentEvidence>()
                            .eq(QcJudgmentEvidence::getJudgmentId, j.getId()));
            if (!fallback.isEmpty()) {
                return fallback;
            }
        }
        return Collections.emptyList();
    }

    /** 是否存在已批准且未过期的让步接收 */
    private boolean hasApprovedConcession(String judgmentId) {
        if (!StringUtils.hasText(judgmentId)) {
            return false;
        }
        Long count = concessionMapper.selectCount(
                new LambdaQueryWrapper<QcConcessionAcceptance>()
                        .eq(QcConcessionAcceptance::getJudgmentId, judgmentId)
                        .eq(QcConcessionAcceptance::getApprovalStatus, "APPROVED")
                        .and(w -> w.isNull(QcConcessionAcceptance::getExpiryDate)
                                .or()
                                .ge(QcConcessionAcceptance::getExpiryDate, LocalDate.now())));
        return count != null && count > 0;
    }

    /**
     * 质保书指标结论：改判合格→PASS；让步已批准或未通过但在让步范围内→CONCESSION；否则 PASS/FAIL。
     */
    private String resolveCertIndicatorResult(
            QcJudgmentEvidence evidence, String finalJudgmentType, boolean concessionApproved) {
        if ("QUALIFIED".equals(finalJudgmentType)) {
            return JudgmentExplainConstants.INDICATOR_RESULT_PASS;
        }
        if (evidence != null && evidence.getIsPassed() != null && evidence.getIsPassed() == 1) {
            return JudgmentExplainConstants.INDICATOR_RESULT_PASS;
        }
        if (concessionApproved) {
            return JudgmentExplainConstants.INDICATOR_RESULT_CONCESSION;
        }
        if (evidence != null
                && StringUtils.hasText(evidence.getTriggerRule())
                && evidence.getTriggerRule().contains(JudgmentExplainConstants.TRIGGER_RULE_CONCESSION_MARKER)) {
            return JudgmentExplainConstants.INDICATOR_RESULT_CONCESSION;
        }
        if (evidence == null) {
            return JudgmentExplainConstants.INDICATOR_RESULT_WARNING;
        }
        return JudgmentExplainConstants.INDICATOR_RESULT_FAIL;
    }

    private int toCertIsPassed(String indicatorResult) {
        return JudgmentExplainConstants.INDICATOR_RESULT_FAIL.equals(indicatorResult) ? 0 : 1;
    }

    /** 详情展示时同步当前最终判定与指标结论（改判/让步后无需重新生成） */
    private void applyCurrentFinalJudgmentToCert(QcQualityCertDataVO vo, QcInspectionRecord record) {
        QcJudgmentResult finalJudgment = findFinalJudgment(record.getId());
        if (finalJudgment == null) {
            return;
        }
        String finalType = finalJudgment.getJudgmentType();
        vo.setFinalJudgmentType(finalType);
        if (vo.getIndicators() == null || vo.getIndicators().isEmpty()) {
            return;
        }
        boolean concessionApproved = hasApprovedConcession(finalJudgment.getId());
        List<QcJudgmentEvidence> evidences = resolveEvidencesForCert(finalJudgment, record.getId());
        Map<String, QcJudgmentEvidence> evidenceByIndicatorId = evidences.stream()
                .collect(Collectors.toMap(QcJudgmentEvidence::getIndicatorId, e -> e, (a, b) -> a));

        for (QcQualityCertDataVO.IndicatorSnapshot snap : vo.getIndicators()) {
            snap.setFinalJudgmentType(finalType);
            QcJudgmentEvidence evidence = findEvidenceForSnapshot(snap, evidenceByIndicatorId);
            String indicatorResult = resolveCertIndicatorResult(evidence, finalType, concessionApproved);
            snap.setIndicatorResult(indicatorResult);
            snap.setIsPassed(toCertIsPassed(indicatorResult));
        }
    }

    private QcJudgmentEvidence findEvidenceForSnapshot(
            QcQualityCertDataVO.IndicatorSnapshot snap, Map<String, QcJudgmentEvidence> evidenceByIndicatorId) {
        if (!StringUtils.hasText(snap.getIndicatorCode())) {
            return null;
        }
        QcIndicatorItem item = indicatorItemMapper.selectOne(
                new LambdaQueryWrapper<QcIndicatorItem>()
                        .eq(QcIndicatorItem::getIndicatorCode, snap.getIndicatorCode())
                        .last("LIMIT 1"));
        if (item == null) {
            return null;
        }
        return evidenceByIndicatorId.get(item.getId());
    }

    private String getLoginUserNo() {
        try {
            Object loginId = StpUtil.getLoginIdDefaultNull();
            return loginId != null ? loginId.toString() : "SYSTEM";
        } catch (Exception e) {
            return "SYSTEM";
        }
    }
}
