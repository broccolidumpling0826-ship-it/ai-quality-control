package com.jhict.quality.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jhict.quality.common.exception.ServiceException;
import com.jhict.quality.dto.QcReinspectionAddCmd;
import com.jhict.quality.dto.QcReinspectionPageQuery;
import com.jhict.quality.dto.ReinspectionCompleteCmd;
import com.jhict.quality.enums.ReinspectionStatus;
import com.jhict.quality.entity.QcInspectionRecord;
import com.jhict.quality.entity.QcJudgmentResult;
import com.jhict.quality.entity.QcReinspectionRecord;
import com.jhict.quality.mapper.QcInspectionRecordMapper;
import com.jhict.quality.mapper.QcJudgmentResultMapper;
import com.jhict.quality.mapper.QcReinspectionRecordMapper;
import com.jhict.quality.service.api.ReinspectionService;
import com.jhict.quality.vo.QcReinspectionListVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ReinspectionServiceImpl implements ReinspectionService {

    /** 单个原判定最多复检次数 */
    private static final int MAX_REINSPECTION_COUNT = 2;

    @Resource
    private QcReinspectionRecordMapper reinspectionMapper;

    @Resource
    private QcJudgmentResultMapper judgmentResultMapper;

    @Resource
    private QcInspectionRecordMapper inspectionRecordMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String initiate(QcReinspectionAddCmd cmd) {
        // 1. 校验原判定结论存在
        QcJudgmentResult judgment = judgmentResultMapper.selectById(cmd.getOriginalJudgmentId());
        if (judgment == null) {
            throw new ServiceException("原判定结论不存在，id=" + cmd.getOriginalJudgmentId());
        }

        // 2. 校验复检次数不超过上限
        long existingCount = reinspectionMapper.selectCount(
                new LambdaQueryWrapper<QcReinspectionRecord>()
                        .eq(QcReinspectionRecord::getOriginalJudgmentId, cmd.getOriginalJudgmentId())
        );
        if (existingCount >= MAX_REINSPECTION_COUNT) {
            throw new ServiceException("该判定结论已发起 " + MAX_REINSPECTION_COUNT + " 次复检，不可再次发起");
        }

        // 3. 保存复检记录
        QcReinspectionRecord record = new QcReinspectionRecord();
        record.setOriginalJudgmentId(cmd.getOriginalJudgmentId());
        record.setReinspectionReason(cmd.getReinspectionReason());
        record.setResponsibleNo(cmd.getResponsibleNo());
        record.setStatus(ReinspectionStatus.PENDING.getCode());
        reinspectionMapper.insert(record);

        log.info("复检记录创建成功，id={}，originalJudgmentId={}", record.getId(), cmd.getOriginalJudgmentId());
        return record.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void complete(String reinspectionId, ReinspectionCompleteCmd cmd) {
        QcReinspectionRecord record = reinspectionMapper.selectById(reinspectionId);
        if (record == null) {
            throw new ServiceException("复检记录不存在，id=" + reinspectionId);
        }
        if (ReinspectionStatus.COMPLETED.getCode().equals(record.getStatus())) {
            throw new ServiceException("复检已完成，不可重复操作");
        }

        record.setNewRecordId(cmd.getNewRecordId());
        record.setStatus(ReinspectionStatus.COMPLETED.getCode());
        reinspectionMapper.updateById(record);

        log.info("复检完成，reinspectionId={}，newRecordId={}", reinspectionId, cmd.getNewRecordId());
    }

    @Override
    public IPage<QcReinspectionListVO> page(QcReinspectionPageQuery query) {
        int pageNum = query.getPageNum() != null && query.getPageNum() > 0 ? query.getPageNum() : 1;
        int pageSize = query.getPageSize() != null && query.getPageSize() > 0 ? query.getPageSize() : 10;

        LambdaQueryWrapper<QcReinspectionRecord> wrapper = new LambdaQueryWrapper<QcReinspectionRecord>()
                .orderByDesc(QcReinspectionRecord::getCreateDateTime);
        if (StringUtils.hasText(query.getStatus())) {
            wrapper.eq(QcReinspectionRecord::getStatus, query.getStatus());
        }
        if (StringUtils.hasText(query.getResponsibleNo())) {
            wrapper.eq(QcReinspectionRecord::getResponsibleNo, query.getResponsibleNo());
        }

        IPage<QcReinspectionRecord> entityPage = reinspectionMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);

        Set<String> judgmentIds = entityPage.getRecords().stream()
                .map(QcReinspectionRecord::getOriginalJudgmentId)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());
        Map<String, QcJudgmentResult> judgmentMap = judgmentIds.isEmpty()
                ? Collections.emptyMap()
                : judgmentResultMapper.selectBatchIds(judgmentIds).stream()
                .collect(Collectors.toMap(QcJudgmentResult::getId, j -> j, (a, b) -> a));

        Set<String> recordIds = judgmentMap.values().stream()
                .map(QcJudgmentResult::getRecordId)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());
        Map<String, QcInspectionRecord> recordMap = recordIds.isEmpty()
                ? Collections.emptyMap()
                : inspectionRecordMapper.selectBatchIds(recordIds).stream()
                .collect(Collectors.toMap(QcInspectionRecord::getId, r -> r, (a, b) -> a));

        List<QcReinspectionListVO> voList = entityPage.getRecords().stream()
                .map(r -> toReinspectionListVO(r, judgmentMap, recordMap))
                .collect(Collectors.toList());

        Page<QcReinspectionListVO> voPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }

    private QcReinspectionListVO toReinspectionListVO(
            QcReinspectionRecord r,
            Map<String, QcJudgmentResult> judgmentMap,
            Map<String, QcInspectionRecord> recordMap) {
        QcReinspectionListVO vo = new QcReinspectionListVO();
        vo.setId(r.getId());
        vo.setOriginalJudgmentId(r.getOriginalJudgmentId());
        vo.setReinspectionReason(r.getReinspectionReason());
        vo.setResponsibleNo(r.getResponsibleNo());
        vo.setStatus(r.getStatus());
        vo.setNewRecordId(r.getNewRecordId());
        vo.setCreateDateTime(r.getCreateDateTime());
        QcJudgmentResult judgment = judgmentMap.get(r.getOriginalJudgmentId());
        if (judgment != null) {
            vo.setOriginalJudgmentType(judgment.getJudgmentType());
            QcInspectionRecord record = recordMap.get(judgment.getRecordId());
            if (record != null) {
                vo.setCoilNo(record.getCoilNo());
            }
        }
        return vo;
    }
}
