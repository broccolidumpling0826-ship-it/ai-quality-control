package com.jhict.quality.service.impl;

import com.alibaba.fastjson2.JSON;
import com.jhict.quality.ai.agent.ReinspectionAdvisor;
import com.jhict.quality.entity.QcAiSuggestion;
import com.jhict.quality.mapper.QcAiSuggestionMapper;
import com.jhict.quality.service.api.AiSuggestionService;
import com.jhict.quality.vo.AiSuggestionVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class AiSuggestionServiceImpl implements AiSuggestionService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Resource
    private ReinspectionAdvisor reinspectionAdvisor;

    @Resource
    private QcAiSuggestionMapper suggestionMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiSuggestionVO getReinspectionSuggestion(String judgmentId) {
        return persistSuggestion(reinspectionAdvisor.advise(judgmentId, "REINSPECTION"));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiSuggestionVO getRejudgmentSuggestion(String judgmentId) {
        return persistSuggestion(reinspectionAdvisor.advise(judgmentId, "REJUDGMENT"));
    }

    private AiSuggestionVO persistSuggestion(AiSuggestionVO vo) {
        QcAiSuggestion entity = new QcAiSuggestion();
        entity.setSuggestionType(vo.getSuggestionType());
        entity.setRefId(vo.getRefId());
        entity.setRecommendedAction(vo.getRecommendedAction());
        entity.setFocusIndicators(JSON.toJSONString(vo.getFocusIndicators()));
        entity.setReasonText(vo.getReasonText());
        entity.setConfidenceLevel(vo.getConfidenceLevel());
        entity.setDegraded(Boolean.TRUE.equals(vo.getDegraded()) ? 1 : 0);
        entity.setAuditLogId(vo.getAuditLogId());
        String now = LocalDateTime.now().format(FORMATTER);
        entity.setCreateDateTime(now);
        entity.setUpdateDateTime(now);
        suggestionMapper.insert(entity);
        vo.setId(entity.getId());
        return vo;
    }
}
