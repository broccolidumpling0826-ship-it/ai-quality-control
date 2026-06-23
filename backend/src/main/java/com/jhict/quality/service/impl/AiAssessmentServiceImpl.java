package com.jhict.quality.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jhict.quality.common.entity.ApiResult;
import com.jhict.quality.common.exception.ServiceException;
import com.jhict.quality.common.annotation.AuditLog;
import com.jhict.quality.dto.AiAssessmentCreateCmd;
import com.jhict.quality.dto.AiAssessmentHandleCmd;
import com.jhict.quality.dto.AiAssessmentPageQuery;
import com.jhict.quality.entity.QcAiAssessment;
import com.jhict.quality.mapper.QcAiAssessmentMapper;
import com.jhict.quality.service.api.AiAssessmentService;
import com.jhict.quality.vo.AiAssessmentVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AiAssessmentServiceImpl implements AiAssessmentService {

    private static final String ADOPTION_PENDING = "PENDING";
    private static final String CONFIDENCE_LOW = "LOW";
    private static final String RISK_HIGH = "HIGH";
    private static final String RISK_BLOCKED = "BLOCKED";
    private static final String RISK_MANUAL_REVIEW = "MANUAL_REVIEW";

    @Resource
    private QcAiAssessmentMapper aiAssessmentMapper;

    @Override
    @AuditLog(operationType = "CREATE_AI_ASSESSMENT", targetEntity = "QcAiAssessment")
    @Transactional(rollbackFor = Exception.class)
    public QcAiAssessment create(AiAssessmentCreateCmd cmd) {
        QcAiAssessment assessment = new QcAiAssessment();
        assessment.setAssessmentType(cmd.getAssessmentType());
        assessment.setBusinessType(cmd.getBusinessType());
        assessment.setBusinessId(cmd.getBusinessId());
        assessment.setRelatedJudgmentId(cmd.getRelatedJudgmentId());
        assessment.setInputSnapshot(cmd.getInputSnapshot());
        assessment.setReferencesJson(cmd.getReferencesJson());
        assessment.setModelProvider(cmd.getModelProvider());
        assessment.setModelName(cmd.getModelName());
        assessment.setPromptVersion(cmd.getPromptVersion());
        assessment.setRawOutput(cmd.getRawOutput());
        assessment.setStructuredOutput(cmd.getStructuredOutput());
        assessment.setRiskLevel(cmd.getRiskLevel());
        assessment.setConfidenceScore(cmd.getConfidenceScore());
        assessment.setConfidenceLabel(cmd.getConfidenceLabel());
        assessment.setConfidenceFactors(cmd.getConfidenceFactors());
        assessment.setDegradationSource(cmd.getDegradationSource());
        assessment.setCacheHit(cmd.getCacheHit() == null ? 0 : cmd.getCacheHit());
        assessment.setCacheKey(cmd.getCacheKey());
        assessment.setAdoptionStatus("PENDING");
        aiAssessmentMapper.insert(assessment);
        return assessment;
    }

    @Override
    public IPage<AiAssessmentVO> page(AiAssessmentPageQuery query) {
        int pageNum = query.getPageNum() != null && query.getPageNum() > 0 ? query.getPageNum() : 1;
        int pageSize = query.getPageSize() != null && query.getPageSize() > 0 ? query.getPageSize() : 20;
        IPage<QcAiAssessment> entityPage = aiAssessmentMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<QcAiAssessment>()
                        .eq(StringUtils.hasText(query.getAssessmentType()), QcAiAssessment::getAssessmentType, query.getAssessmentType())
                        .eq(StringUtils.hasText(query.getBusinessType()), QcAiAssessment::getBusinessType, query.getBusinessType())
                        .eq(StringUtils.hasText(query.getBusinessId()), QcAiAssessment::getBusinessId, query.getBusinessId())
                        .eq(StringUtils.hasText(query.getRelatedJudgmentId()), QcAiAssessment::getRelatedJudgmentId, query.getRelatedJudgmentId())
                        .eq(StringUtils.hasText(query.getRiskLevel()), QcAiAssessment::getRiskLevel, query.getRiskLevel())
                        .eq(StringUtils.hasText(query.getConfidenceLabel()), QcAiAssessment::getConfidenceLabel, query.getConfidenceLabel())
                        .eq(StringUtils.hasText(query.getDegradationSource()), QcAiAssessment::getDegradationSource, query.getDegradationSource())
                        .eq(StringUtils.hasText(query.getAdoptionStatus()), QcAiAssessment::getAdoptionStatus, query.getAdoptionStatus())
                        .orderByDesc(QcAiAssessment::getCreateDateTime));
        Page<AiAssessmentVO> voPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        voPage.setRecords(entityPage.getRecords().stream().map(this::toVO).collect(Collectors.toList()));
        return voPage;
    }

    @Override
    public AiAssessmentVO getById(String id) {
        QcAiAssessment assessment = aiAssessmentMapper.selectById(id);
        if (assessment == null) {
            throw new ServiceException(ApiResult.CODE_NOT_FOUND, "AI评估记录不存在");
        }
        return toVO(assessment);
    }

    @Override
    @AuditLog(operationType = "HANDLE_AI_ASSESSMENT", targetEntity = "QcAiAssessment")
    public QcAiAssessment handle(String id, AiAssessmentHandleCmd cmd) {
        if (!"ADOPTED".equals(cmd.getAdoptionStatus()) && !"IGNORED".equals(cmd.getAdoptionStatus())) {
            throw new ServiceException(ApiResult.CODE_BAD_REQUEST, "处理状态仅支持 ADOPTED/IGNORED");
        }
        QcAiAssessment assessment = aiAssessmentMapper.selectById(id);
        if (assessment == null) {
            throw new ServiceException(ApiResult.CODE_NOT_FOUND, "AI评估记录不存在");
        }
        assessment.setAdoptionStatus(cmd.getAdoptionStatus());
        assessment.setHumanOpinion(cmd.getHumanOpinion());
        assessment.setHandledBy(currentLoginId());
        assessment.setHandledTime(LocalDateTime.now());
        aiAssessmentMapper.updateById(assessment);
        return assessment;
    }

    @Override
    public Long countPendingRiskWarnings() {
        Long count = aiAssessmentMapper.selectCount(new LambdaQueryWrapper<QcAiAssessment>()
                .eq(QcAiAssessment::getAdoptionStatus, ADOPTION_PENDING)
                .in(QcAiAssessment::getRiskLevel, RISK_HIGH, RISK_BLOCKED, RISK_MANUAL_REVIEW));
        return count == null ? 0L : count;
    }

    @Override
    public Long countPendingLowConfidenceReviews() {
        Long count = aiAssessmentMapper.selectCount(new LambdaQueryWrapper<QcAiAssessment>()
                .eq(QcAiAssessment::getAdoptionStatus, ADOPTION_PENDING)
                .eq(QcAiAssessment::getConfidenceLabel, CONFIDENCE_LOW));
        return count == null ? 0L : count;
    }

    @Override
    public Long countAssessments(String timeStart, String timeEnd) {
        Long count = aiAssessmentMapper.selectCount(applyTimeRange(new LambdaQueryWrapper<QcAiAssessment>(),
                timeStart, timeEnd));
        return count == null ? 0L : count;
    }

    @Override
    public Long countAdoptedAssessments(String timeStart, String timeEnd) {
        Long count = aiAssessmentMapper.selectCount(applyTimeRange(new LambdaQueryWrapper<QcAiAssessment>()
                        .eq(QcAiAssessment::getAdoptionStatus, "ADOPTED"),
                timeStart, timeEnd));
        return count == null ? 0L : count;
    }

    @Override
    public Long countHandledAssessments(String timeStart, String timeEnd) {
        Long count = aiAssessmentMapper.selectCount(applyTimeRange(new LambdaQueryWrapper<QcAiAssessment>()
                        .in(QcAiAssessment::getAdoptionStatus, "ADOPTED", "IGNORED"),
                timeStart, timeEnd));
        return count == null ? 0L : count;
    }

    @Override
    public Long countLowConfidenceAssessments(String timeStart, String timeEnd) {
        Long count = aiAssessmentMapper.selectCount(applyTimeRange(new LambdaQueryWrapper<QcAiAssessment>()
                        .eq(QcAiAssessment::getConfidenceLabel, CONFIDENCE_LOW),
                timeStart, timeEnd));
        return count == null ? 0L : count;
    }

    @Override
    public Optional<QcAiAssessment> findLatestByRelatedJudgment(String relatedJudgmentId, String assessmentType) {
        if (!StringUtils.hasText(relatedJudgmentId) || !StringUtils.hasText(assessmentType)) {
            return Optional.empty();
        }
        QcAiAssessment assessment = aiAssessmentMapper.selectOne(new LambdaQueryWrapper<QcAiAssessment>()
                .eq(QcAiAssessment::getRelatedJudgmentId, relatedJudgmentId)
                .eq(QcAiAssessment::getAssessmentType, assessmentType)
                .orderByDesc(QcAiAssessment::getCreateDateTime)
                .last("LIMIT 1"));
        return Optional.ofNullable(assessment);
    }

    private LambdaQueryWrapper<QcAiAssessment> applyTimeRange(LambdaQueryWrapper<QcAiAssessment> wrapper,
                                                             String timeStart,
                                                             String timeEnd) {
        wrapper.ge(StringUtils.hasText(timeStart), QcAiAssessment::getCreateDateTime, timeStart);
        wrapper.le(StringUtils.hasText(timeEnd), QcAiAssessment::getCreateDateTime, timeEnd);
        return wrapper;
    }

    private AiAssessmentVO toVO(QcAiAssessment assessment) {
        AiAssessmentVO vo = new AiAssessmentVO();
        vo.setId(assessment.getId());
        vo.setAssessmentType(assessment.getAssessmentType());
        vo.setBusinessType(assessment.getBusinessType());
        vo.setBusinessId(assessment.getBusinessId());
        vo.setRelatedJudgmentId(assessment.getRelatedJudgmentId());
        vo.setInputSnapshot(assessment.getInputSnapshot());
        vo.setReferencesJson(assessment.getReferencesJson());
        vo.setModelProvider(assessment.getModelProvider());
        vo.setModelName(assessment.getModelName());
        vo.setPromptVersion(assessment.getPromptVersion());
        vo.setRawOutput(assessment.getRawOutput());
        vo.setStructuredOutput(assessment.getStructuredOutput());
        vo.setRiskLevel(assessment.getRiskLevel());
        vo.setConfidenceScore(assessment.getConfidenceScore());
        vo.setConfidenceLabel(assessment.getConfidenceLabel());
        vo.setConfidenceFactors(assessment.getConfidenceFactors());
        vo.setDegradationSource(assessment.getDegradationSource());
        vo.setCacheHit(assessment.getCacheHit());
        vo.setCacheKey(assessment.getCacheKey());
        vo.setAdoptionStatus(assessment.getAdoptionStatus());
        vo.setHumanOpinion(assessment.getHumanOpinion());
        vo.setHandledBy(assessment.getHandledBy());
        vo.setHandledTime(assessment.getHandledTime());
        vo.setCreateUserNo(assessment.getCreateUserNo());
        vo.setCreateDateTime(assessment.getCreateDateTime());
        return vo;
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
