package com.jhict.quality.service.impl;

import com.jhict.quality.ai.agent.ConcessionAgent;
import com.jhict.quality.dto.ConcessionAssessCmd;
import com.jhict.quality.entity.QcConcessionAssessment;
import com.jhict.quality.mapper.QcConcessionAssessmentMapper;
import com.jhict.quality.service.api.AiConcessionService;
import com.jhict.quality.vo.ConcessionAssessmentVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class AiConcessionServiceImpl implements AiConcessionService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Resource
    private ConcessionAgent concessionAgent;

    @Resource
    private QcConcessionAssessmentMapper assessmentMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ConcessionAssessmentVO assess(ConcessionAssessCmd cmd) {
        ConcessionAssessmentVO vo = concessionAgent.assess(cmd.getJudgmentId(), cmd.getConcessionId());

        QcConcessionAssessment entity = new QcConcessionAssessment();
        entity.setJudgmentId(cmd.getJudgmentId());
        entity.setConcessionId(cmd.getConcessionId());
        entity.setRiskLevel(vo.getRiskLevel());
        entity.setCustomerImpact(vo.getCustomerImpact());
        entity.setSuggestedConditions(vo.getSuggestedConditions());
        entity.setHistoricalCases(vo.getHistoricalCases());
        entity.setConfidenceLevel(vo.getConfidenceLevel());
        entity.setDegraded(Boolean.TRUE.equals(vo.getDegraded()) ? 1 : 0);
        entity.setAuditLogId(vo.getAuditLogId());
        String now = LocalDateTime.now().format(FORMATTER);
        entity.setCreateDateTime(now);
        entity.setUpdateDateTime(now);
        assessmentMapper.insert(entity);

        vo.setId(entity.getId());
        return vo;
    }
}
