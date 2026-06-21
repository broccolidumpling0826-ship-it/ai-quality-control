package com.jhict.quality.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jhict.quality.common.annotation.AuditLog;
import com.jhict.quality.common.entity.ApiResult;
import com.jhict.quality.common.exception.ServiceException;
import com.jhict.quality.dto.StandardConflictCreateCmd;
import com.jhict.quality.dto.StandardConflictPageQuery;
import com.jhict.quality.dto.StandardConflictResolveCmd;
import com.jhict.quality.entity.StandardConflict;
import com.jhict.quality.mapper.StandardConflictMapper;
import com.jhict.quality.service.api.StandardConflictService;
import com.jhict.quality.vo.StandardConflictVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class StandardConflictServiceImpl implements StandardConflictService {

    private static final String LEVEL_BLOCKING = "BLOCKING";
    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_RESOLVED = "RESOLVED";
    private static final DateTimeFormatter NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Resource
    private StandardConflictMapper standardConflictMapper;

    @Override
    @AuditLog(operationType = "SAVE_STANDARD_CONFLICT", targetEntity = "StandardConflict")
    @Transactional(rollbackFor = Exception.class)
    public List<StandardConflict> saveDetectedConflicts(String recordId, String judgmentId,
                                                        List<StandardConflictCreateCmd> conflicts) {
        List<StandardConflict> saved = new ArrayList<>();
        if (conflicts == null || conflicts.isEmpty()) {
            return saved;
        }

        for (StandardConflictCreateCmd cmd : conflicts) {
            StandardConflict conflict = new StandardConflict();
            conflict.setConflictNo(nextConflictNo());
            conflict.setJudgmentId(StringUtils.hasText(cmd.getJudgmentId()) ? cmd.getJudgmentId() : judgmentId);
            conflict.setRecordId(StringUtils.hasText(cmd.getRecordId()) ? cmd.getRecordId() : recordId);
            conflict.setConflictType(cmd.getConflictType());
            conflict.setConflictLevel(cmd.getConflictLevel());
            conflict.setStatus(resolveInitialStatus(cmd));
            conflict.setIndicatorId(cmd.getIndicatorId());
            conflict.setIndicatorName(cmd.getIndicatorName());
            conflict.setUnit(cmd.getUnit());
            conflict.setCustomerId(cmd.getCustomerId());
            conflict.setVariety(cmd.getVariety());
            conflict.setGrade(cmd.getGrade());
            conflict.setProductSpec(cmd.getProductSpec());
            conflict.setInspectionDate(cmd.getInspectionDate());
            conflict.setSelectedStandardId(cmd.getSelectedStandardId());
            conflict.setInvolvedStandardIds(toJson(cmd.getInvolvedStandardIds()));
            conflict.setConflictDetail(cmd.getConflictDetail());
            conflict.setSelectedPriority(cmd.getSelectedPriority());
            standardConflictMapper.insert(conflict);
            saved.add(conflict);
        }

        log.info("保存标准冲突记录，recordId={}, judgmentId={}, count={}", recordId, judgmentId, saved.size());
        return saved;
    }

    @Override
    public IPage<StandardConflictVO> page(StandardConflictPageQuery query) {
        int pageNum = query.getPageNum() != null && query.getPageNum() > 0 ? query.getPageNum() : 1;
        int pageSize = query.getPageSize() != null && query.getPageSize() > 0 ? query.getPageSize() : 20;
        Page<StandardConflict> page = new Page<>(pageNum, pageSize);
        IPage<StandardConflict> entityPage = standardConflictMapper.selectPage(page,
                new LambdaQueryWrapper<StandardConflict>()
                        .eq(StringUtils.hasText(query.getRecordId()), StandardConflict::getRecordId, query.getRecordId())
                        .eq(StringUtils.hasText(query.getJudgmentId()), StandardConflict::getJudgmentId, query.getJudgmentId())
                        .eq(StringUtils.hasText(query.getConflictLevel()), StandardConflict::getConflictLevel, query.getConflictLevel())
                        .eq(StringUtils.hasText(query.getStatus()), StandardConflict::getStatus, query.getStatus())
                        .eq(StringUtils.hasText(query.getCustomerId()), StandardConflict::getCustomerId, query.getCustomerId())
                        .eq(StringUtils.hasText(query.getVariety()), StandardConflict::getVariety, query.getVariety())
                        .eq(StringUtils.hasText(query.getGrade()), StandardConflict::getGrade, query.getGrade())
                        .orderByDesc(StandardConflict::getCreateDateTime));
        Page<StandardConflictVO> voPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        voPage.setRecords(entityPage.getRecords().stream().map(this::toVO).collect(Collectors.toList()));
        return voPage;
    }

    @Override
    public StandardConflictVO getById(String id) {
        StandardConflict conflict = standardConflictMapper.selectById(id);
        if (conflict == null) {
            throw new ServiceException(ApiResult.CODE_NOT_FOUND, "标准冲突不存在");
        }
        return toVO(conflict);
    }

    @Override
    @AuditLog(operationType = "RESOLVE_STANDARD_CONFLICT", targetEntity = "StandardConflict")
    @Transactional(rollbackFor = Exception.class)
    public StandardConflict resolveDecision(String id, StandardConflictResolveCmd cmd) {
        StandardConflict conflict = standardConflictMapper.selectById(id);
        if (conflict == null) {
            throw new ServiceException(ApiResult.CODE_NOT_FOUND, "标准冲突不存在");
        }
        if (!LEVEL_BLOCKING.equals(conflict.getConflictLevel())) {
            throw new ServiceException(ApiResult.CODE_BAD_REQUEST, "仅阻断标准冲突需要人工裁决");
        }
        if (!STATUS_PENDING.equals(conflict.getStatus())) {
            throw new ServiceException(ApiResult.CODE_BAD_REQUEST, "当前冲突状态不可裁决：" + conflict.getStatus());
        }
        List<String> involvedIds = parseInvolvedStandardIds(conflict.getInvolvedStandardIds());
        if (!involvedIds.contains(cmd.getDecisionStandardId())) {
            throw new ServiceException(ApiResult.CODE_BAD_REQUEST, "裁决控制标准必须来自冲突涉及标准");
        }

        conflict.setStatus(STATUS_RESOLVED);
        conflict.setDecisionStandardId(cmd.getDecisionStandardId());
        conflict.setSelectedStandardId(cmd.getDecisionStandardId());
        conflict.setDecisionReason(cmd.getDecisionReason());
        conflict.setDecisionBy(currentLoginId());
        conflict.setDecisionTime(LocalDateTime.now());
        standardConflictMapper.updateById(conflict);
        log.info("完成标准冲突裁决，conflictId={}, decisionStandardId={}", id, cmd.getDecisionStandardId());
        return conflict;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindRejudgeJudgment(String id, String rejudgeJudgmentId) {
        StandardConflict conflict = standardConflictMapper.selectById(id);
        if (conflict == null) {
            throw new ServiceException(ApiResult.CODE_NOT_FOUND, "标准冲突不存在");
        }
        conflict.setRejudgeJudgmentId(rejudgeJudgmentId);
        standardConflictMapper.updateById(conflict);
    }

    @Override
    public boolean hasUnresolvedBlockingConflict(String recordId) {
        if (!StringUtils.hasText(recordId)) {
            return false;
        }
        Long count = standardConflictMapper.selectCount(new LambdaQueryWrapper<StandardConflict>()
                .eq(StandardConflict::getRecordId, recordId)
                .eq(StandardConflict::getConflictLevel, LEVEL_BLOCKING)
                .eq(StandardConflict::getStatus, STATUS_PENDING));
        return count != null && count > 0;
    }

    @Override
    public Long countPendingBlockingConflicts() {
        Long count = standardConflictMapper.selectCount(new LambdaQueryWrapper<StandardConflict>()
                .eq(StandardConflict::getConflictLevel, LEVEL_BLOCKING)
                .eq(StandardConflict::getStatus, STATUS_PENDING));
        return count == null ? 0L : count;
    }

    @Override
    public Long countConflicts(String timeStart, String timeEnd) {
        Long count = standardConflictMapper.selectCount(new LambdaQueryWrapper<StandardConflict>()
                .ge(StringUtils.hasText(timeStart), StandardConflict::getCreateDateTime, timeStart)
                .le(StringUtils.hasText(timeEnd), StandardConflict::getCreateDateTime, timeEnd));
        return count == null ? 0L : count;
    }

    @Override
    public Long countResolvedConflicts(String timeStart, String timeEnd) {
        Long count = standardConflictMapper.selectCount(new LambdaQueryWrapper<StandardConflict>()
                .eq(StandardConflict::getStatus, STATUS_RESOLVED)
                .ge(StringUtils.hasText(timeStart), StandardConflict::getCreateDateTime, timeStart)
                .le(StringUtils.hasText(timeEnd), StandardConflict::getCreateDateTime, timeEnd));
        return count == null ? 0L : count;
    }

    private String resolveInitialStatus(StandardConflictCreateCmd cmd) {
        if (StringUtils.hasText(cmd.getStatus())) {
            return cmd.getStatus();
        }
        return LEVEL_BLOCKING.equals(cmd.getConflictLevel()) ? STATUS_PENDING : STATUS_RESOLVED;
    }

    private String nextConflictNo() {
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return "SCF-" + LocalDateTime.now().format(NO_FORMATTER) + "-" + suffix;
    }

    private String toJson(List<String> ids) {
        try {
            return objectMapper.writeValueAsString(ids == null ? new ArrayList<String>() : ids);
        } catch (Exception e) {
            return "[]";
        }
    }

    private StandardConflictVO toVO(StandardConflict conflict) {
        StandardConflictVO vo = new StandardConflictVO();
        vo.setId(conflict.getId());
        vo.setConflictNo(conflict.getConflictNo());
        vo.setJudgmentId(conflict.getJudgmentId());
        vo.setRecordId(conflict.getRecordId());
        vo.setConflictType(conflict.getConflictType());
        vo.setConflictLevel(conflict.getConflictLevel());
        vo.setStatus(conflict.getStatus());
        vo.setIndicatorId(conflict.getIndicatorId());
        vo.setIndicatorName(conflict.getIndicatorName());
        vo.setUnit(conflict.getUnit());
        vo.setCustomerId(conflict.getCustomerId());
        vo.setVariety(conflict.getVariety());
        vo.setGrade(conflict.getGrade());
        vo.setProductSpec(conflict.getProductSpec());
        vo.setInspectionDate(conflict.getInspectionDate());
        vo.setSelectedStandardId(conflict.getSelectedStandardId());
        vo.setInvolvedStandardIds(parseInvolvedStandardIds(conflict.getInvolvedStandardIds()));
        vo.setConflictDetail(conflict.getConflictDetail());
        vo.setSelectedPriority(conflict.getSelectedPriority());
        vo.setDecisionStandardId(conflict.getDecisionStandardId());
        vo.setDecisionReason(conflict.getDecisionReason());
        vo.setDecisionBy(conflict.getDecisionBy());
        vo.setDecisionTime(conflict.getDecisionTime());
        vo.setRejudgeJudgmentId(conflict.getRejudgeJudgmentId());
        vo.setCreateDateTime(conflict.getCreateDateTime());
        return vo;
    }

    private List<String> parseInvolvedStandardIds(String json) {
        if (!StringUtils.hasText(json)) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {
            });
        } catch (Exception e) {
            log.warn("解析冲突涉及标准失败，json={}", json);
            return new ArrayList<>();
        }
    }

    private String currentLoginId() {
        try {
            Object loginId = StpUtil.getLoginIdDefaultNull();
            return loginId == null ? "SYSTEM" : loginId.toString();
        } catch (Exception e) {
            return "SYSTEM";
        }
    }
}
