package com.jhict.quality.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jhict.quality.common.annotation.AuditLog;
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
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CertDataServiceImpl implements CertDataService {

    private static final String LIST_STATUS_CASE = "CASE WHEN snapshot_data IS NOT NULL "
            + "AND TRIM(snapshot_data) <> '[]' AND CHAR_LENGTH(snapshot_data) > 2 "
            + "THEN 'SUCCESS' ELSE 'FAILED' END AS list_status";

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

    @Value("${app.cert.included-categories:COMPOSITION,PERFORMANCE,DIMENSION}")
    private String includedCategoriesConfig;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public QcQualityCertDataVO generate(QcQualityCertGenerateCmd cmd) {
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

        List<String> allRecordIds = records.stream().map(QcInspectionRecord::getId).collect(Collectors.toList());
        Map<String, QcJudgmentResult> finalJudgmentByRecordId = loadFinalJudgmentsMap(allRecordIds);
        records = pickLatestRecordsForCert(records, finalJudgmentByRecordId);

        List<String> recordIds = records.stream().map(QcInspectionRecord::getId).collect(Collectors.toList());
        Map<String, QcJudgmentResult> finalJudgmentMap = loadFinalJudgmentsMap(recordIds);
        Set<String> approvedJudgmentIds = loadApprovedConcessionJudgmentIds(
                finalJudgmentMap.values().stream().map(QcJudgmentResult::getId).collect(Collectors.toSet()));
        validateGenerateGates(records, finalJudgmentMap, approvedJudgmentIds);

        Map<String, List<QcInspectionValue>> valuesByRecordId = loadValuesByRecordIds(recordIds);
        Map<String, List<QcJudgmentResult>> judgmentsByRecordId = loadJudgmentsByRecordIds(recordIds);
        Map<String, List<QcJudgmentEvidence>> evidencesByJudgmentId = loadEvidencesByJudgmentIds(
                collectAllJudgmentIds(judgmentsByRecordId));

        Set<String> indicatorIds = valuesByRecordId.values().stream()
                .flatMap(List::stream)
                .map(QcInspectionValue::getIndicatorId)
                .collect(Collectors.toSet());
        Map<String, QcIndicatorItem> indicatorMap = loadIndicatorMap(indicatorIds);

        Set<String> includedCategories = new HashSet<>(Arrays.asList(includedCategoriesConfig.split(",")));
        List<QcQualityCertDataVO.IndicatorSnapshot> snapshots = new ArrayList<>();

        for (QcInspectionRecord record : records) {
            QcJudgmentResult judgment = finalJudgmentMap.get(record.getId());
            String finalJudgmentType = judgment != null ? judgment.getJudgmentType() : null;
            boolean concessionApproved = judgment != null && approvedJudgmentIds.contains(judgment.getId());

            List<QcJudgmentEvidence> evidences = resolveEvidencesForCert(
                    judgment, record.getId(), judgmentsByRecordId, evidencesByJudgmentId);
            Map<String, QcJudgmentEvidence> evidenceByIndicator = evidences.stream()
                    .collect(Collectors.toMap(QcJudgmentEvidence::getIndicatorId, e -> e, (a, b) -> a));

            List<QcInspectionValue> values = valuesByRecordId.getOrDefault(record.getId(), Collections.emptyList());
            for (QcInspectionValue value : values) {
                QcIndicatorItem indicator = indicatorMap.get(value.getIndicatorId());
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
        if (snapshots.isEmpty()) {
            throw new ServiceException("缺少可纳入质保书的关键检验指标，禁止生成正式质保书数据");
        }

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
        return toDetailVO(certData);
    }

    @Override
    public QcQualityCertDataVO getById(String id) {
        QcQualityCertData data = certDataMapper.selectById(id);
        if (data == null) {
            throw new ServiceException("质保书数据不存在");
        }
        return toDetailVO(data);
    }

    @Override
    public IPage<QcQualityCertDataVO> page(int pageNum, int pageSize, String coilNo, String batchNo,
                                           String startTime, String endTime) {
        QueryWrapper<QcQualityCertData> wrapper = new QueryWrapper<>();
        wrapper.select(
                "id",
                "coil_no",
                "batch_no",
                "generate_time",
                "generated_by",
                LIST_STATUS_CASE
        );
        wrapper.orderByDesc("generate_time");
        if (StringUtils.hasText(coilNo)) {
            wrapper.eq("coil_no", coilNo);
        }
        if (StringUtils.hasText(batchNo)) {
            wrapper.eq("batch_no", batchNo);
        }
        applyGenerateTimeRange(wrapper, startTime, endTime);

        IPage<QcQualityCertData> pageResult = certDataMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        List<QcQualityCertDataVO> voList = pageResult.getRecords().stream()
                .map(this::toListVO)
                .collect(Collectors.toList());
        batchEnrichListVo(voList);

        Page<QcQualityCertDataVO> voPage = new Page<>(pageResult.getCurrent(), pageResult.getSize(), pageResult.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    @AuditLog(operationType = "EXPORT_CERT_PDF", targetEntity = "QcQualityCertData")
    public byte[] exportPdf(String id) {
        QcQualityCertDataVO vo = getById(id);
        validateFormalExportGate(vo);
        return buildSimplePdf(vo);
    }

    private void validateGenerateGates(List<QcInspectionRecord> records,
                                       Map<String, QcJudgmentResult> finalJudgmentMap,
                                       Set<String> approvedJudgmentIds) {
        for (QcInspectionRecord record : records) {
            QcJudgmentResult judgment = finalJudgmentMap.get(record.getId());
            if (judgment == null) {
                throw new ServiceException("检验记录 " + record.getId() + " 尚无最终判定，禁止生成正式质保书数据");
            }
            validateJudgmentReleasable(judgment, approvedJudgmentIds);
        }
    }

    private void validateJudgmentReleasable(QcJudgmentResult judgment, Set<String> approvedJudgmentIds) {
        String type = judgment.getJudgmentType();
        if ("QUALIFIED".equals(type)) {
            return;
        }
        if ("CAN_CONCESSION".equals(type) || "CONCESSION".equals(type)) {
            if (approvedJudgmentIds.contains(judgment.getId())) {
                return;
            }
            throw new ServiceException("可让步判定尚未完成让步审批和客户确认，禁止生成正式质保书");
        }
        if ("STANDARD_CONFLICT".equals(type)) {
            throw new ServiceException("存在未解决标准冲突，禁止生成正式质保书");
        }
        if ("NEED_REINSPECTION".equals(type) || "REINSPECTION".equals(type)) {
            throw new ServiceException("判定需要复检，复检完成前禁止生成正式质保书");
        }
        if ("UNQUALIFIED".equals(type)) {
            throw new ServiceException("最终判定不合格，禁止生成正式质保书");
        }
        throw new ServiceException("未知或不可放行判定类型：" + type);
    }

    private void validateFormalExportGate(QcQualityCertDataVO vo) {
        if (!"SUCCESS".equals(vo.getStatus()) || vo.getIndicators() == null || vo.getIndicators().isEmpty()) {
            throw new ServiceException("质保书快照不完整，禁止导出正式PDF");
        }
        QcInspectionRecord record = resolvePrimaryInspectionRecordForCert(vo.getCoilNo(), vo.getBatchNo());
        if (record == null) {
            throw new ServiceException("未找到质保书对应检验记录，禁止导出正式PDF");
        }
        QcJudgmentResult judgment = loadFinalJudgmentsMap(Collections.singletonList(record.getId())).get(record.getId());
        if (judgment == null) {
            throw new ServiceException("未找到最终判定，禁止导出正式PDF");
        }
        validateJudgmentReleasable(judgment, loadApprovedConcessionJudgmentIds(Collections.singleton(judgment.getId())));
        boolean hasBlockingIndicator = vo.getIndicators().stream()
                .anyMatch(i -> JudgmentExplainConstants.INDICATOR_RESULT_FAIL.equals(i.getIndicatorResult())
                        || JudgmentExplainConstants.INDICATOR_RESULT_WARNING.equals(i.getIndicatorResult()));
        if (hasBlockingIndicator) {
            throw new ServiceException("质保书存在失败或缺失指标，禁止导出正式PDF");
        }
    }

    private byte[] buildSimplePdf(QcQualityCertDataVO vo) {
        List<String> lines = new ArrayList<>();
        lines.add("QUALITY CERTIFICATE");
        lines.add("Cert Data ID: " + nullToDash(vo.getId()));
        lines.add("Coil No: " + nullToDash(vo.getCoilNo()));
        lines.add("Batch No: " + nullToDash(vo.getBatchNo()));
        lines.add("Heat No: " + nullToDash(vo.getHeatNo()));
        lines.add("Product: " + nullToDash(vo.getProductVariety()) + " / " + nullToDash(vo.getProductGrade()));
        lines.add("Final Judgment: " + nullToDash(vo.getFinalJudgmentType()));
        lines.add("Generated At: " + (vo.getGenerateTime() == null ? "-" : vo.getGenerateTime()));
        lines.add("Generated By: " + nullToDash(vo.getGeneratedBy()));
        lines.add(" ");
        lines.add("Indicators:");
        for (QcQualityCertDataVO.IndicatorSnapshot item : vo.getIndicators()) {
            lines.add(String.format(Locale.ROOT, "%s %s %s limit[%s,%s] result=%s",
                    nullToDash(item.getIndicatorCode()),
                    decimalToString(item.getTestValue()),
                    nullToDash(item.getUnit()),
                    decimalToString(item.getLowerLimit()),
                    decimalToString(item.getUpperLimit()),
                    nullToDash(item.getIndicatorResult())));
        }

        StringBuilder content = new StringBuilder();
        content.append("BT\n/F1 11 Tf\n50 790 Td\n14 TL\n");
        for (String line : lines) {
            content.append("(").append(escapePdfAscii(line)).append(") Tj\nT*\n");
        }
        content.append("ET\n");

        String contentStream = content.toString();
        List<String> objects = new ArrayList<>();
        objects.add("<< /Type /Catalog /Pages 2 0 R >>");
        objects.add("<< /Type /Pages /Kids [3 0 R] /Count 1 >>");
        objects.add("<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Resources << /Font << /F1 4 0 R >> >> /Contents 5 0 R >>");
        objects.add("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>");
        objects.add("<< /Length " + contentStream.getBytes(StandardCharsets.US_ASCII).length
                + " >>\nstream\n" + contentStream + "endstream");

        StringBuilder pdf = new StringBuilder("%PDF-1.4\n");
        List<Integer> offsets = new ArrayList<>();
        for (int i = 0; i < objects.size(); i++) {
            offsets.add(pdf.toString().getBytes(StandardCharsets.US_ASCII).length);
            pdf.append(i + 1).append(" 0 obj\n").append(objects.get(i)).append("\nendobj\n");
        }
        int xrefOffset = pdf.toString().getBytes(StandardCharsets.US_ASCII).length;
        pdf.append("xref\n0 ").append(objects.size() + 1).append("\n");
        pdf.append("0000000000 65535 f \n");
        for (Integer offset : offsets) {
            pdf.append(String.format(Locale.ROOT, "%010d 00000 n \n", offset));
        }
        pdf.append("trailer\n<< /Size ").append(objects.size() + 1).append(" /Root 1 0 R >>\n");
        pdf.append("startxref\n").append(xrefOffset).append("\n%%EOF\n");
        return pdf.toString().getBytes(StandardCharsets.US_ASCII);
    }

    private String escapePdfAscii(String value) {
        String ascii = value == null ? "-" : value.replaceAll("[^\\x20-\\x7E]", "?");
        return ascii.replace("\\", "\\\\")
                .replace("(", "\\(")
                .replace(")", "\\)");
    }

    private String decimalToString(java.math.BigDecimal value) {
        return value == null ? "-" : value.stripTrailingZeros().toPlainString();
    }

    private String nullToDash(String value) {
        return StringUtils.hasText(value) ? value : "-";
    }

    private void applyGenerateTimeRange(QueryWrapper<QcQualityCertData> wrapper,
                                        String startTime, String endTime) {
        if (StringUtils.hasText(startTime)) {
            wrapper.ge("generate_time", LocalDate.parse(startTime).atStartOfDay());
        }
        if (StringUtils.hasText(endTime)) {
            wrapper.le("generate_time", LocalDate.parse(endTime).atTime(23, 59, 59));
        }
    }

    /** 列表 VO：不解析 snapshot、不同步改判，仅展示元数据 + 炉号 */
    private QcQualityCertDataVO toListVO(QcQualityCertData data) {
        QcQualityCertDataVO vo = new QcQualityCertDataVO();
        vo.setId(data.getId());
        vo.setCoilNo(data.getCoilNo());
        vo.setBatchNo(data.getBatchNo());
        vo.setGenerateTime(data.getGenerateTime());
        vo.setGeneratedBy(data.getGeneratedBy());
        vo.setStatus(StringUtils.hasText(data.getListStatus()) ? data.getListStatus() : "FAILED");
        vo.setIndicators(null);
        return vo;
    }

    /** 批量补全列表炉号（避免逐条查检验记录） */
    private void batchEnrichListVo(List<QcQualityCertDataVO> voList) {
        if (voList.isEmpty()) {
            return;
        }
        Set<String> coilNos = voList.stream()
                .map(QcQualityCertDataVO::getCoilNo)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());
        Set<String> batchNos = voList.stream()
                .map(QcQualityCertDataVO::getBatchNo)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());
        if (coilNos.isEmpty() && batchNos.isEmpty()) {
            return;
        }

        LambdaQueryWrapper<QcInspectionRecord> wrapper = new LambdaQueryWrapper<QcInspectionRecord>()
                .eq(QcInspectionRecord::getStatus, "NORMAL");
        wrapper.and(w -> {
            if (!coilNos.isEmpty()) {
                w.in(QcInspectionRecord::getCoilNo, coilNos);
            }
            if (!batchNos.isEmpty()) {
                if (!coilNos.isEmpty()) {
                    w.or();
                }
                w.in(QcInspectionRecord::getBatchNo, batchNos);
            }
        });
        List<QcInspectionRecord> records = inspectionRecordMapper.selectList(wrapper);
        if (records.isEmpty()) {
            return;
        }

        List<String> recordIds = records.stream().map(QcInspectionRecord::getId).collect(Collectors.toList());
        Map<String, QcJudgmentResult> finalJudgmentByRecordId = loadFinalJudgmentsMap(recordIds);

        Map<String, QcInspectionRecord> bestByCoil = new HashMap<>();
        Map<String, QcInspectionRecord> bestByBatch = new HashMap<>();
        Map<String, List<QcInspectionRecord>> byCoil = records.stream()
                .filter(r -> StringUtils.hasText(r.getCoilNo()))
                .collect(Collectors.groupingBy(QcInspectionRecord::getCoilNo));
        Map<String, List<QcInspectionRecord>> byBatch = records.stream()
                .filter(r -> StringUtils.hasText(r.getBatchNo()))
                .collect(Collectors.groupingBy(QcInspectionRecord::getBatchNo));

        for (Map.Entry<String, List<QcInspectionRecord>> e : byCoil.entrySet()) {
            bestByCoil.put(e.getKey(), resolveBestRecordByFinalJudgment(e.getValue(), finalJudgmentByRecordId));
        }
        for (Map.Entry<String, List<QcInspectionRecord>> e : byBatch.entrySet()) {
            bestByBatch.put(e.getKey(), resolveBestRecordByFinalJudgment(e.getValue(), finalJudgmentByRecordId));
        }

        for (QcQualityCertDataVO vo : voList) {
            QcInspectionRecord record = null;
            if (StringUtils.hasText(vo.getCoilNo())) {
                record = bestByCoil.get(vo.getCoilNo());
            }
            if (record == null && StringUtils.hasText(vo.getBatchNo())) {
                record = bestByBatch.get(vo.getBatchNo());
            }
            if (record != null) {
                vo.setHeatNo(record.getHeatNo());
            }
        }
    }

    private QcQualityCertDataVO toDetailVO(QcQualityCertData data) {
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

    private void enrichVoFromInspection(QcQualityCertDataVO vo, QcQualityCertData data) {
        QcInspectionRecord record = resolvePrimaryInspectionRecordForCert(data.getCoilNo(), data.getBatchNo());
        if (record == null) {
            vo.setStatus(vo.getIndicators() != null && !vo.getIndicators().isEmpty() ? "SUCCESS" : "FAILED");
            return;
        }
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

    private List<QcInspectionRecord> pickLatestRecordsForCert(List<QcInspectionRecord> records,
                                                              Map<String, QcJudgmentResult> finalJudgmentByRecordId) {
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
            picked.add(resolveBestRecordByFinalJudgment(group, finalJudgmentByRecordId));
        }
        return picked;
    }

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
        Map<String, QcJudgmentResult> judgmentMap = loadFinalJudgmentsMap(
                records.stream().map(QcInspectionRecord::getId).collect(Collectors.toList()));
        return resolveBestRecordByFinalJudgment(records, judgmentMap);
    }

    private QcInspectionRecord resolveBestRecordByFinalJudgment(List<QcInspectionRecord> records,
                                                                Map<String, QcJudgmentResult> finalJudgmentByRecordId) {
        QcInspectionRecord bestRecord = null;
        LocalDateTime bestJudgmentTime = null;
        for (QcInspectionRecord record : records) {
            QcJudgmentResult finalJudgment = finalJudgmentByRecordId.get(record.getId());
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

    private Map<String, QcJudgmentResult> loadFinalJudgmentsMap(Collection<String> recordIds) {
        if (recordIds == null || recordIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<QcJudgmentResult> list = judgmentResultMapper.selectList(
                new LambdaQueryWrapper<QcJudgmentResult>()
                        .in(QcJudgmentResult::getRecordId, recordIds)
                        .eq(QcJudgmentResult::getIsFinal, 1));
        return list.stream().collect(Collectors.toMap(
                QcJudgmentResult::getRecordId,
                j -> j,
                (a, b) -> compareJudgmentTime(a, b) >= 0 ? a : b));
    }

    private Map<String, List<QcInspectionValue>> loadValuesByRecordIds(Collection<String> recordIds) {
        if (recordIds == null || recordIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<QcInspectionValue> values = inspectionValueMapper.selectList(
                new LambdaQueryWrapper<QcInspectionValue>()
                        .in(QcInspectionValue::getRecordId, recordIds));
        return values.stream().collect(Collectors.groupingBy(QcInspectionValue::getRecordId));
    }

    private Map<String, List<QcJudgmentResult>> loadJudgmentsByRecordIds(Collection<String> recordIds) {
        if (recordIds == null || recordIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<QcJudgmentResult> list = judgmentResultMapper.selectList(
                new LambdaQueryWrapper<QcJudgmentResult>()
                        .in(QcJudgmentResult::getRecordId, recordIds)
                        .orderByDesc(QcJudgmentResult::getJudgmentTime));
        return list.stream().collect(Collectors.groupingBy(QcJudgmentResult::getRecordId));
    }

    private Set<String> collectAllJudgmentIds(Map<String, List<QcJudgmentResult>> judgmentsByRecordId) {
        return judgmentsByRecordId.values().stream()
                .flatMap(List::stream)
                .map(QcJudgmentResult::getId)
                .collect(Collectors.toSet());
    }

    private Map<String, List<QcJudgmentEvidence>> loadEvidencesByJudgmentIds(Collection<String> judgmentIds) {
        if (judgmentIds == null || judgmentIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<QcJudgmentEvidence> list = judgmentEvidenceMapper.selectList(
                new LambdaQueryWrapper<QcJudgmentEvidence>()
                        .in(QcJudgmentEvidence::getJudgmentId, judgmentIds));
        return list.stream().collect(Collectors.groupingBy(QcJudgmentEvidence::getJudgmentId));
    }

    private Map<String, QcIndicatorItem> loadIndicatorMap(Collection<String> indicatorIds) {
        if (indicatorIds == null || indicatorIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return indicatorItemMapper.selectBatchIds(indicatorIds).stream()
                .collect(Collectors.toMap(QcIndicatorItem::getId, i -> i, (a, b) -> a));
    }

    private Set<String> loadApprovedConcessionJudgmentIds(Collection<String> judgmentIds) {
        if (judgmentIds == null || judgmentIds.isEmpty()) {
            return Collections.emptySet();
        }
        List<QcConcessionAcceptance> list = concessionMapper.selectList(
                new LambdaQueryWrapper<QcConcessionAcceptance>()
                        .in(QcConcessionAcceptance::getJudgmentId, judgmentIds)
                        .eq(QcConcessionAcceptance::getApprovalStatus, "APPROVED")
                        .and(w -> w.isNull(QcConcessionAcceptance::getExpiryDate)
                                .or()
                                .ge(QcConcessionAcceptance::getExpiryDate, LocalDate.now())));
        return list.stream()
                .map(QcConcessionAcceptance::getJudgmentId)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());
    }

    private List<QcJudgmentEvidence> resolveEvidencesForCert(
            QcJudgmentResult judgment,
            String recordId,
            Map<String, List<QcJudgmentResult>> judgmentsByRecordId,
            Map<String, List<QcJudgmentEvidence>> evidencesByJudgmentId) {
        if (judgment != null) {
            List<QcJudgmentEvidence> current = evidencesByJudgmentId.getOrDefault(
                    judgment.getId(), Collections.emptyList());
            if (!current.isEmpty()) {
                return current;
            }
        }
        List<QcJudgmentResult> history = judgmentsByRecordId.getOrDefault(recordId, Collections.emptyList());
        for (QcJudgmentResult j : history) {
            List<QcJudgmentEvidence> fallback = evidencesByJudgmentId.getOrDefault(
                    j.getId(), Collections.emptyList());
            if (!fallback.isEmpty()) {
                return fallback;
            }
        }
        return Collections.emptyList();
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

    private boolean hasApprovedConcession(String judgmentId) {
        if (!StringUtils.hasText(judgmentId)) {
            return false;
        }
        return loadApprovedConcessionJudgmentIds(Collections.singletonList(judgmentId)).contains(judgmentId);
    }

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

    private void applyCurrentFinalJudgmentToCert(QcQualityCertDataVO vo, QcInspectionRecord record) {
        Map<String, QcJudgmentResult> judgmentMap = loadFinalJudgmentsMap(Collections.singletonList(record.getId()));
        QcJudgmentResult finalJudgment = judgmentMap.get(record.getId());
        if (finalJudgment == null) {
            return;
        }
        String finalType = finalJudgment.getJudgmentType();
        vo.setFinalJudgmentType(finalType);
        if (vo.getIndicators() == null || vo.getIndicators().isEmpty()) {
            return;
        }
        boolean concessionApproved = hasApprovedConcession(finalJudgment.getId());
        Map<String, List<QcJudgmentResult>> judgmentsByRecordId = loadJudgmentsByRecordIds(
                Collections.singletonList(record.getId()));
        Map<String, List<QcJudgmentEvidence>> evidencesByJudgmentId = loadEvidencesByJudgmentIds(
                collectAllJudgmentIds(judgmentsByRecordId));
        List<QcJudgmentEvidence> evidences = resolveEvidencesForCert(
                finalJudgment, record.getId(), judgmentsByRecordId, evidencesByJudgmentId);
        Map<String, QcJudgmentEvidence> evidenceByIndicatorId = evidences.stream()
                .collect(Collectors.toMap(QcJudgmentEvidence::getIndicatorId, e -> e, (a, b) -> a));

        Map<String, QcIndicatorItem> indicatorByCode = loadIndicatorsByCodes(
                vo.getIndicators().stream()
                        .map(QcQualityCertDataVO.IndicatorSnapshot::getIndicatorCode)
                        .filter(StringUtils::hasText)
                        .collect(Collectors.toSet()));

        for (QcQualityCertDataVO.IndicatorSnapshot snap : vo.getIndicators()) {
            snap.setFinalJudgmentType(finalType);
            QcJudgmentEvidence evidence = findEvidenceForSnapshot(snap, evidenceByIndicatorId, indicatorByCode);
            String indicatorResult = resolveCertIndicatorResult(evidence, finalType, concessionApproved);
            snap.setIndicatorResult(indicatorResult);
            snap.setIsPassed(toCertIsPassed(indicatorResult));
        }
    }

    private Map<String, QcIndicatorItem> loadIndicatorsByCodes(Set<String> codes) {
        if (codes == null || codes.isEmpty()) {
            return Collections.emptyMap();
        }
        List<QcIndicatorItem> items = indicatorItemMapper.selectList(
                new LambdaQueryWrapper<QcIndicatorItem>()
                        .in(QcIndicatorItem::getIndicatorCode, codes));
        return items.stream()
                .collect(Collectors.toMap(QcIndicatorItem::getIndicatorCode, i -> i, (a, b) -> a));
    }

    private QcJudgmentEvidence findEvidenceForSnapshot(
            QcQualityCertDataVO.IndicatorSnapshot snap,
            Map<String, QcJudgmentEvidence> evidenceByIndicatorId,
            Map<String, QcIndicatorItem> indicatorByCode) {
        if (!StringUtils.hasText(snap.getIndicatorCode())) {
            return null;
        }
        QcIndicatorItem item = indicatorByCode.get(snap.getIndicatorCode());
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
