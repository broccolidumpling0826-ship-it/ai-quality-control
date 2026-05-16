package com.jhict.quality.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jhict.quality.entity.QcConcessionAcceptance;
import com.jhict.quality.entity.QcInspectionRecord;
import com.jhict.quality.entity.QcJudgmentResult;
import com.jhict.quality.entity.QcReinspectionRecord;
import com.jhict.quality.entity.SysNotification;
import com.jhict.quality.mapper.QcConcessionAcceptanceMapper;
import com.jhict.quality.mapper.QcInspectionRecordMapper;
import com.jhict.quality.mapper.QcJudgmentResultMapper;
import com.jhict.quality.mapper.QcReinspectionRecordMapper;
import com.jhict.quality.mapper.SysNotificationMapper;
import com.jhict.quality.service.api.DashboardService;
import com.jhict.quality.vo.DashboardMessageVO;
import com.jhict.quality.vo.DashboardPendingItemVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DashboardServiceImpl implements DashboardService {

    private static final int PENDING_LIMIT_PER_TYPE = 10;
    private static final int MESSAGE_LIMIT = 20;

    @Resource
    private QcInspectionRecordMapper inspectionRecordMapper;

    @Resource
    private QcJudgmentResultMapper judgmentResultMapper;

    @Resource
    private QcReinspectionRecordMapper reinspectionRecordMapper;

    @Resource
    private QcConcessionAcceptanceMapper concessionMapper;

    @Resource
    private SysNotificationMapper notificationMapper;

    @Override
    public List<DashboardPendingItemVO> listPendingItems(String userNo) {
        List<DashboardPendingItemVO> items = new ArrayList<>();
        items.addAll(loadPendingJudgments());
        items.addAll(loadPendingReinspections());
        items.addAll(loadPendingConcessions());

        items.sort(Comparator.comparing(DashboardPendingItemVO::getCreateTime,
                Comparator.nullsLast(Comparator.reverseOrder())));
        return items;
    }

    @Override
    public List<DashboardMessageVO> listMessages(String userNo) {
        LambdaQueryWrapper<SysNotification> wrapper = new LambdaQueryWrapper<SysNotification>()
                .eq(SysNotification::getReceiverNo, userNo)
                .orderByDesc(SysNotification::getCreateDateTime)
                .last("LIMIT " + MESSAGE_LIMIT);

        return notificationMapper.selectList(wrapper).stream()
                .map(this::toMessageVo)
                .collect(Collectors.toList());
    }

    private List<DashboardPendingItemVO> loadPendingJudgments() {
        List<QcInspectionRecord> records = inspectionRecordMapper.selectList(
                new LambdaQueryWrapper<QcInspectionRecord>()
                        .eq(QcInspectionRecord::getStatus, "NORMAL")
                        .orderByDesc(QcInspectionRecord::getCreateDateTime)
                        .last("LIMIT " + (PENDING_LIMIT_PER_TYPE * 3)));

        if (records.isEmpty()) {
            return new ArrayList<>();
        }

        Set<String> recordIds = records.stream().map(QcInspectionRecord::getId).collect(Collectors.toSet());
        Set<String> judgedRecordIds = judgmentResultMapper.selectList(
                new LambdaQueryWrapper<QcJudgmentResult>()
                        .in(QcJudgmentResult::getRecordId, recordIds)
                        .eq(QcJudgmentResult::getIsFinal, 1)
        ).stream().map(QcJudgmentResult::getRecordId).collect(Collectors.toSet());

        List<DashboardPendingItemVO> result = new ArrayList<>();
        for (QcInspectionRecord record : records) {
            if (judgedRecordIds.contains(record.getId())) {
                continue;
            }
            DashboardPendingItemVO item = new DashboardPendingItemVO();
            item.setId(record.getId());
            item.setType("judgment");
            item.setBatchNo(record.getBatchNo());
            item.setDescription(String.format("卷号 %s %s %s 待判定",
                    nullToEmpty(record.getCoilNo()),
                    nullToEmpty(record.getProductVariety()),
                    nullToEmpty(record.getProductGrade())));
            item.setPriority("high");
            item.setCreateTime(record.getCreateDateTime());
            item.setAssignTo(nullToEmpty(record.getTesterNo()));
            result.add(item);
            if (result.size() >= PENDING_LIMIT_PER_TYPE) {
                break;
            }
        }
        return result;
    }

    private List<DashboardPendingItemVO> loadPendingReinspections() {
        List<QcReinspectionRecord> list = reinspectionRecordMapper.selectList(
                new LambdaQueryWrapper<QcReinspectionRecord>()
                        .eq(QcReinspectionRecord::getStatus, "PENDING")
                        .orderByDesc(QcReinspectionRecord::getCreateDateTime)
                        .last("LIMIT " + PENDING_LIMIT_PER_TYPE));

        if (list.isEmpty()) {
            return new ArrayList<>();
        }

        Set<String> judgmentIds = list.stream()
                .map(QcReinspectionRecord::getOriginalJudgmentId)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());

        Map<String, QcJudgmentResult> judgmentMap = judgmentIds.isEmpty()
                ? java.util.Collections.emptyMap()
                : judgmentResultMapper.selectBatchIds(judgmentIds).stream()
                .collect(Collectors.toMap(QcJudgmentResult::getId, j -> j, (a, b) -> a));

        Set<String> recordIds = judgmentMap.values().stream()
                .map(QcJudgmentResult::getRecordId)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());

        Map<String, QcInspectionRecord> recordMap = recordIds.isEmpty()
                ? java.util.Collections.emptyMap()
                : inspectionRecordMapper.selectBatchIds(recordIds).stream()
                .collect(Collectors.toMap(QcInspectionRecord::getId, r -> r, (a, b) -> a));

        List<DashboardPendingItemVO> result = new ArrayList<>();
        for (QcReinspectionRecord ri : list) {
            QcJudgmentResult judgment = judgmentMap.get(ri.getOriginalJudgmentId());
            QcInspectionRecord record = judgment != null ? recordMap.get(judgment.getRecordId()) : null;

            DashboardPendingItemVO item = new DashboardPendingItemVO();
            item.setId(ri.getId());
            item.setType("reinspection");
            item.setBatchNo(record != null ? record.getBatchNo() : "-");
            item.setDescription(StringUtils.hasText(ri.getReinspectionReason())
                    ? ri.getReinspectionReason()
                    : "复检任务待完成");
            item.setPriority("medium");
            item.setCreateTime(ri.getCreateDateTime());
            item.setAssignTo(nullToEmpty(ri.getResponsibleNo()));
            result.add(item);
        }
        return result;
    }

    private List<DashboardPendingItemVO> loadPendingConcessions() {
        List<QcConcessionAcceptance> list = concessionMapper.selectList(
                new LambdaQueryWrapper<QcConcessionAcceptance>()
                        .in(QcConcessionAcceptance::getApprovalStatus, "PENDING", "PENDING_APPROVAL")
                        .orderByDesc(QcConcessionAcceptance::getCreateDateTime)
                        .last("LIMIT " + PENDING_LIMIT_PER_TYPE));

        if (list.isEmpty()) {
            return new ArrayList<>();
        }

        Set<String> judgmentIds = list.stream()
                .map(QcConcessionAcceptance::getJudgmentId)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());

        Map<String, QcJudgmentResult> judgmentMap = judgmentIds.isEmpty()
                ? java.util.Collections.emptyMap()
                : judgmentResultMapper.selectBatchIds(judgmentIds).stream()
                .collect(Collectors.toMap(QcJudgmentResult::getId, j -> j, (a, b) -> a));

        Set<String> recordIds = new HashSet<>();
        for (QcJudgmentResult j : judgmentMap.values()) {
            if (StringUtils.hasText(j.getRecordId())) {
                recordIds.add(j.getRecordId());
            }
        }

        Map<String, QcInspectionRecord> recordMap = recordIds.isEmpty()
                ? java.util.Collections.emptyMap()
                : inspectionRecordMapper.selectBatchIds(recordIds).stream()
                .collect(Collectors.toMap(QcInspectionRecord::getId, r -> r, (a, b) -> a));

        List<DashboardPendingItemVO> result = new ArrayList<>();
        for (QcConcessionAcceptance c : list) {
            QcJudgmentResult judgment = judgmentMap.get(c.getJudgmentId());
            QcInspectionRecord record = judgment != null ? recordMap.get(judgment.getRecordId()) : null;

            DashboardPendingItemVO item = new DashboardPendingItemVO();
            item.setId(c.getId());
            item.setType("concession");
            item.setBatchNo(record != null ? record.getBatchNo() : "-");
            item.setDescription(StringUtils.hasText(c.getConcessionScope())
                    ? "让步审批：" + c.getConcessionScope()
                    : "让步接收待审批");
            item.setPriority("high");
            item.setCreateTime(c.getCreateDateTime());
            item.setAssignTo(nullToEmpty(c.getCreateUserNo()));
            result.add(item);
        }
        return result;
    }

    private DashboardMessageVO toMessageVo(SysNotification n) {
        DashboardMessageVO vo = new DashboardMessageVO();
        vo.setId(n.getId());
        vo.setTitle(n.getTitle());
        vo.setContent(n.getContent());
        vo.setLevel(resolveMessageLevel(n.getRelatedType()));
        vo.setCreateTime(n.getCreateDateTime());
        vo.setIsRead(n.getIsRead() != null && n.getIsRead() == 1);
        return vo;
    }

    private String resolveMessageLevel(String relatedType) {
        if (!StringUtils.hasText(relatedType)) {
            return "info";
        }
        switch (relatedType) {
            case "CONCESSION_EXPIRE":
            case "CONCESSION_REMIND":
                return "warning";
            case "CONCESSION_INVALID":
            case "STANDARD_GAP":
                return "error";
            default:
                return "info";
        }
    }

    private String nullToEmpty(String value) {
        return value != null ? value : "";
    }
}
