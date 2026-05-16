package com.jhict.quality.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jhict.quality.common.exception.ServiceException;
import com.jhict.quality.dto.QcQualityCertGenerateCmd;
import com.jhict.quality.entity.*;
import com.jhict.quality.mapper.*;
import com.jhict.quality.service.api.CertDataService;
import com.jhict.quality.vo.QcQualityCertDataVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
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

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String generate(QcQualityCertGenerateCmd cmd) {
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

        // 2. 收集记录ID，查询最终判定和检验值
        List<String> recordIds = records.stream().map(QcInspectionRecord::getId).collect(Collectors.toList());

        // 查最终判定结论（isFinal=1）
        Map<String, QcJudgmentResult> finalJudgmentMap = judgmentResultMapper.selectList(
                new LambdaQueryWrapper<QcJudgmentResult>()
                        .in(QcJudgmentResult::getRecordId, recordIds)
                        .eq(QcJudgmentResult::getIsFinal, 1)
        ).stream().collect(Collectors.toMap(QcJudgmentResult::getRecordId, j -> j, (a, b) -> a));

        // 3. 组装 indicator 快照
        List<QcQualityCertDataVO.IndicatorSnapshot> snapshots = new ArrayList<>();

        for (QcInspectionRecord record : records) {
            QcJudgmentResult judgment = finalJudgmentMap.get(record.getId());
            String finalJudgmentType = judgment != null ? judgment.getJudgmentType() : null;

            // 查检验值
            List<QcInspectionValue> values = inspectionValueMapper.selectList(
                    new LambdaQueryWrapper<QcInspectionValue>()
                            .eq(QcInspectionValue::getRecordId, record.getId())
            );

            // 查判定依据（快照含上下限）
            List<QcJudgmentEvidence> evidences = judgment != null
                    ? judgmentEvidenceMapper.selectList(
                            new LambdaQueryWrapper<QcJudgmentEvidence>()
                                    .eq(QcJudgmentEvidence::getJudgmentId, judgment.getId()))
                    : Collections.emptyList();

            Map<String, QcJudgmentEvidence> evidenceByIndicator = evidences.stream()
                    .collect(Collectors.toMap(QcJudgmentEvidence::getIndicatorId, e -> e, (a, b) -> a));

            for (QcInspectionValue value : values) {
                QcIndicatorItem indicator = indicatorItemMapper.selectById(value.getIndicatorId());
                QcJudgmentEvidence evidence = evidenceByIndicator.get(value.getIndicatorId());

                QcQualityCertDataVO.IndicatorSnapshot snap = new QcQualityCertDataVO.IndicatorSnapshot();
                snap.setIndicatorName(indicator != null ? indicator.getIndicatorName() : value.getIndicatorId());
                snap.setUnit(indicator != null ? indicator.getUnit() : null);
                snap.setTestValue(value.getTestValue());
                snap.setUpperLimit(evidence != null ? evidence.getUpperLimit() : null);
                snap.setLowerLimit(evidence != null ? evidence.getLowerLimit() : null);
                snap.setIsPassed(evidence != null ? evidence.getIsPassed() : null);
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
        QcQualityCertData certData = new QcQualityCertData();
        certData.setCoilNo(cmd.getCoilNo());
        certData.setBatchNo(cmd.getBatchNo());
        certData.setSnapshotData(snapshotJson);
        certData.setGenerateTime(LocalDateTime.now());
        certData.setGeneratedBy(generatedBy);
        certDataMapper.insert(certData);

        log.info("质保书数据生成成功，id={}，generatedBy={}", certData.getId(), generatedBy);
        return certData.getId();
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
        return vo;
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
