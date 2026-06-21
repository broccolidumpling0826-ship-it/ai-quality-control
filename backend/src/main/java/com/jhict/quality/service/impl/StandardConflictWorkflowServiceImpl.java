package com.jhict.quality.service.impl;

import com.jhict.quality.common.annotation.AuditLog;
import com.jhict.quality.dto.StandardConflictResolveCmd;
import com.jhict.quality.entity.QcJudgmentResult;
import com.jhict.quality.entity.StandardConflict;
import com.jhict.quality.service.api.InspectionService;
import com.jhict.quality.service.api.StandardConflictService;
import com.jhict.quality.service.api.StandardConflictWorkflowService;
import com.jhict.quality.vo.StandardConflictVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Slf4j
@Service
public class StandardConflictWorkflowServiceImpl implements StandardConflictWorkflowService {

    @Resource
    private StandardConflictService standardConflictService;

    @Resource
    private InspectionService inspectionService;

    @Override
    @AuditLog(operationType = "RESOLVE_STANDARD_CONFLICT_WORKFLOW", targetEntity = "StandardConflict")
    @Transactional(rollbackFor = Exception.class)
    public StandardConflictVO resolveAndRejudge(String conflictId, StandardConflictResolveCmd cmd) {
        StandardConflict conflict = standardConflictService.resolveDecision(conflictId, cmd);
        QcJudgmentResult rejudgeResult = inspectionService.rejudgeWithStandard(
                conflict.getRecordId(), cmd.getDecisionStandardId());
        standardConflictService.bindRejudgeJudgment(conflictId, rejudgeResult.getId());
        log.info("标准冲突裁决后重判完成，conflictId={}, rejudgeJudgmentId={}",
                conflictId, rejudgeResult.getId());
        return standardConflictService.getById(conflictId);
    }
}
