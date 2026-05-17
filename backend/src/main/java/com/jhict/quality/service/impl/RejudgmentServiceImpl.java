package com.jhict.quality.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jhict.quality.common.annotation.AuditLog;
import com.jhict.quality.common.exception.ServiceException;
import com.jhict.quality.common.util.AuthUtils;
import com.jhict.quality.dto.QcRejudgmentRequestAddCmd;
import com.jhict.quality.dto.RejudgmentApproveCmd;
import com.jhict.quality.entity.QcConcessionAcceptance;
import com.jhict.quality.entity.QcInspectionRecord;
import com.jhict.quality.entity.QcJudgmentEvidence;
import com.jhict.quality.entity.QcJudgmentResult;
import com.jhict.quality.entity.QcRejudgmentApproval;
import com.jhict.quality.entity.QcRejudgmentRequest;
import com.jhict.quality.entity.SysUser;
import com.jhict.quality.mapper.QcConcessionAcceptanceMapper;
import com.jhict.quality.mapper.QcInspectionRecordMapper;
import com.jhict.quality.mapper.QcJudgmentEvidenceMapper;
import com.jhict.quality.mapper.QcJudgmentResultMapper;
import com.jhict.quality.mapper.QcRejudgmentApprovalMapper;
import com.jhict.quality.mapper.QcRejudgmentRequestMapper;
import com.jhict.quality.mapper.SysUserMapper;
import com.jhict.quality.service.api.NotificationService;
import com.jhict.quality.service.api.RejudgmentService;
import com.jhict.quality.vo.QcRejudgmentListVO;
import com.jhict.quality.vo.QcRejudgmentRequestVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RejudgmentServiceImpl implements RejudgmentService {

    private static final String DASHBOARD_CACHE_PATTERN = "dashboard:*";

    /** 逆向改判：原结论为合格/可让步，改为不合格/需复检 */
    private static final List<String> POSITIVE_TYPES = Arrays.asList("QUALIFIED", "CAN_CONCESSION");
    private static final List<String> NEGATIVE_TYPES = Arrays.asList("UNQUALIFIED", "NEED_REINSPECTION");

    @Resource
    private QcRejudgmentRequestMapper rejudgmentRequestMapper;

    @Resource
    private QcRejudgmentApprovalMapper rejudgmentApprovalMapper;

    @Resource
    private QcJudgmentResultMapper judgmentResultMapper;

    @Resource
    private QcJudgmentEvidenceMapper judgmentEvidenceMapper;

    @Resource
    private QcInspectionRecordMapper inspectionRecordMapper;

    @Resource
    private SysUserMapper sysUserMapper;

    @Resource
    private QcConcessionAcceptanceMapper concessionAcceptanceMapper;

    @Resource
    private NotificationService notificationService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String apply(QcRejudgmentRequestAddCmd cmd) {
        // 1. 查原判定结论
        QcJudgmentResult original = judgmentResultMapper.selectById(cmd.getOriginalJudgmentId());
        if (original == null) {
            throw new ServiceException("原判定结论不存在，id=" + cmd.getOriginalJudgmentId());
        }

        // 2. 自动判断是否逆向改判
        String originalType = original.getJudgmentType();
        String targetType = cmd.getTargetJudgmentType();
        int isReverse = (POSITIVE_TYPES.contains(originalType) && NEGATIVE_TYPES.contains(targetType)) ? 1 : 0;

        // 3. 逆向改判必须提供新证据
        if (isReverse == 1) {
            if (!StringUtils.hasText(cmd.getNewEvidenceSource())) {
                throw new ServiceException("逆向改判必须填写新证据来源");
            }
            if (!StringUtils.hasText(cmd.getEvidenceAttachmentUrl())) {
                throw new ServiceException("逆向改判必须上传证据附件");
            }
        }

        // 4. 确定审批层级
        String approvalLevel = isReverse == 1 ? "ENHANCED" : "NORMAL";

        // 5. 保存申请
        QcRejudgmentRequest request = new QcRejudgmentRequest();
        request.setOriginalJudgmentId(cmd.getOriginalJudgmentId());
        request.setOriginalJudgmentType(originalType);
        request.setTargetJudgmentType(targetType);
        request.setRejudgmentReason(cmd.getRejudgmentReason());
        request.setAffectScope(cmd.getAffectScope());
        request.setIsReverse(isReverse);
        request.setNewEvidenceSource(cmd.getNewEvidenceSource());
        request.setEvidenceAttachmentUrl(cmd.getEvidenceAttachmentUrl());
        request.setApprovalLevel(approvalLevel);
        request.setApprovalStatus("PENDING");
        rejudgmentRequestMapper.insert(request);

        log.info("改判申请创建成功，id={}，isReverse={}，approvalLevel={}", request.getId(), isReverse, approvalLevel);
        return request.getId();
    }

    @Override
    @AuditLog(operationType = "APPROVE_REJUDGMENT", targetEntity = "QcRejudgmentRequest")
    @Transactional(rollbackFor = Exception.class)
    public void approve(String requestId, RejudgmentApproveCmd cmd) {
        // 1. 查申请
        QcRejudgmentRequest request = rejudgmentRequestMapper.selectById(requestId);
        if (request == null) {
            throw new ServiceException("改判申请不存在，id=" + requestId);
        }
        if (!"PENDING".equals(request.getApprovalStatus())) {
            throw new ServiceException("改判申请当前状态不可审批，status=" + request.getApprovalStatus());
        }

        // 2. 校验审批人角色（ENHANCED 需要 QUALITY_MANAGER；ADMIN 跳过）
        if (!AuthUtils.isAdmin() && "ENHANCED".equals(request.getApprovalLevel())
                && !AuthUtils.hasRole("QUALITY_MANAGER")) {
            throw new ServiceException("逆向改判（加强审批）需要质量经理权限");
        }

        String approverNo = StpUtil.getLoginIdAsString();

        // 3. 保存审批记录
        QcRejudgmentApproval approval = new QcRejudgmentApproval();
        approval.setRequestId(requestId);
        approval.setApproverNo(approverNo);
        approval.setApprovalAction(cmd.getAction());
        approval.setApprovalComment(cmd.getComment());
        approval.setApprovalTime(LocalDateTime.now());
        rejudgmentApprovalMapper.insert(approval);

        // 4. 更新申请状态
        request.setApprovalStatus(cmd.getAction());
        rejudgmentRequestMapper.updateById(request);

        // 5. 审批通过时处理业务逻辑
        if ("APPROVED".equals(cmd.getAction())) {
            // 原判定结论设为非最终
            QcJudgmentResult original = judgmentResultMapper.selectById(request.getOriginalJudgmentId());
            if (original != null) {
                original.setIsFinal(0);
                judgmentResultMapper.updateById(original);

                // 新建目标判定结论
                QcJudgmentResult newJudgment = new QcJudgmentResult();
                newJudgment.setRecordId(original.getRecordId());
                newJudgment.setJudgmentType(request.getTargetJudgmentType());
                newJudgment.setJudgmentTime(LocalDateTime.now());
                newJudgment.setIsFinal(1);
                newJudgment.setRemark("改判申请[" + requestId + "]审批通过后自动生成");
                judgmentResultMapper.insert(newJudgment);
                copyJudgmentEvidences(original.getId(), newJudgment.getId(), request.getTargetJudgmentType());

                // 批量作废关联让步申请（APPROVED 或 PENDING_APPROVAL 状态）
                List<QcConcessionAcceptance> concessions = concessionAcceptanceMapper.selectList(
                        new LambdaQueryWrapper<QcConcessionAcceptance>()
                                .eq(QcConcessionAcceptance::getJudgmentId, request.getOriginalJudgmentId())
                                .in(QcConcessionAcceptance::getApprovalStatus, Arrays.asList("APPROVED", "PENDING"))
                );
                if (!concessions.isEmpty()) {
                    concessions.forEach(c -> {
                        c.setApprovalStatus("INVALID");
                        c.setVoidReason("质量结论已变更，让步基础消失");
                    });
                    concessions.forEach(concessionAcceptanceMapper::updateById);
                    log.info("已批量作废 {} 条让步申请，原因：质量结论变更，originalJudgmentId={}",
                            concessions.size(), request.getOriginalJudgmentId());
                }
            }

            // 清除 Dashboard Redis 缓存
            try {
                Set<String> keys = stringRedisTemplate.keys(DASHBOARD_CACHE_PATTERN);
                if (keys != null && !keys.isEmpty()) {
                    stringRedisTemplate.delete(keys);
                    log.info("已清除 Dashboard 缓存，共 {} 个 key", keys.size());
                }
            } catch (Exception e) {
                log.warn("清除 Dashboard 缓存失败", e);
            }
        }

        // 6. 给申请人发站内通知
        String applicantNo = request.getCreateUserNo();
        if (StringUtils.hasText(applicantNo)) {
            String actionLabel = "APPROVED".equals(cmd.getAction()) ? "已批准" : "已拒绝";
            notificationService.send(
                    applicantNo,
                    "改判申请审批结果通知",
                    "您提交的改判申请（ID：" + requestId + "）" + actionLabel
                            + (StringUtils.hasText(cmd.getComment()) ? "，审批意见：" + cmd.getComment() : ""),
                    "REJUDGMENT",
                    requestId
            );
        }

        log.info("改判审批完成，requestId={}，action={}，approverNo={}", requestId, cmd.getAction(), approverNo);
    }

    @Override
    public IPage<QcRejudgmentListVO> page(int pageNum, int pageSize, String approvalStatus, Integer isReverse) {
        LambdaQueryWrapper<QcRejudgmentRequest> wrapper = new LambdaQueryWrapper<QcRejudgmentRequest>()
                .orderByDesc(QcRejudgmentRequest::getCreateDateTime);
        if (StringUtils.hasText(approvalStatus)) {
            wrapper.eq(QcRejudgmentRequest::getApprovalStatus, approvalStatus);
        }
        if (isReverse != null) {
            wrapper.eq(QcRejudgmentRequest::getIsReverse, isReverse);
        }
        IPage<QcRejudgmentRequest> entityPage = rejudgmentRequestMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);

        Set<String> judgmentIds = entityPage.getRecords().stream()
                .map(QcRejudgmentRequest::getOriginalJudgmentId)
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

        List<QcRejudgmentListVO> voList = entityPage.getRecords().stream()
                .map(r -> toRejudgmentListVO(r, judgmentMap, recordMap))
                .collect(Collectors.toList());

        Page<QcRejudgmentListVO> voPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }

    private QcRejudgmentListVO toRejudgmentListVO(
            QcRejudgmentRequest request,
            Map<String, QcJudgmentResult> judgmentMap,
            Map<String, QcInspectionRecord> recordMap) {
        QcRejudgmentListVO vo = new QcRejudgmentListVO();
        vo.setId(request.getId());
        vo.setOriginalJudgmentType(request.getOriginalJudgmentType());
        vo.setTargetJudgmentType(request.getTargetJudgmentType());
        vo.setRejudgmentReason(request.getRejudgmentReason());
        vo.setIsReverse(request.getIsReverse());
        vo.setApprovalLevel(request.getApprovalLevel());
        vo.setApprovalStatus(request.getApprovalStatus());
        vo.setCreateDateTime(request.getCreateDateTime());
        vo.setCreateUserNo(request.getCreateUserNo());

        QcJudgmentResult judgment = judgmentMap.get(request.getOriginalJudgmentId());
        if (judgment != null && StringUtils.hasText(judgment.getRecordId())) {
            QcInspectionRecord record = recordMap.get(judgment.getRecordId());
            if (record != null) {
                vo.setCoilNo(record.getCoilNo());
                vo.setBatchNo(record.getBatchNo());
            }
        }
        return vo;
    }

    @Override
    public QcRejudgmentRequestVO getById(String id) {
        QcRejudgmentRequest request = rejudgmentRequestMapper.selectById(id);
        if (request == null) {
            throw new ServiceException("改判申请不存在，id=" + id);
        }

        // 查审批记录
        List<QcRejudgmentApproval> approvals = rejudgmentApprovalMapper.selectList(
                new LambdaQueryWrapper<QcRejudgmentApproval>()
                        .eq(QcRejudgmentApproval::getRequestId, id)
                        .orderByAsc(QcRejudgmentApproval::getApprovalTime)
        );

        QcRejudgmentRequestVO vo = new QcRejudgmentRequestVO();
        vo.setId(request.getId());
        vo.setOriginalJudgmentId(request.getOriginalJudgmentId());
        vo.setOriginalJudgmentType(request.getOriginalJudgmentType());
        vo.setTargetJudgmentType(request.getTargetJudgmentType());
        vo.setRejudgmentReason(request.getRejudgmentReason());
        vo.setReason(request.getRejudgmentReason());
        vo.setAffectScope(request.getAffectScope());
        vo.setImpactScope(request.getAffectScope());
        vo.setEvidenceSource(request.getNewEvidenceSource());
        vo.setEvidenceFileUrl(request.getEvidenceAttachmentUrl());
        vo.setIsReverse(request.getIsReverse());
        vo.setApprovalLevel(request.getApprovalLevel());
        vo.setApprovalStatus(request.getApprovalStatus());
        vo.setIsReverseLabel(Integer.valueOf(1).equals(request.getIsReverse()) ? "逆向改判" : "");
        vo.setApplyTime(request.getCreateDateTime());
        vo.setApplyBy(request.getCreateUserNo());
        if (StringUtils.hasText(request.getCreateUserNo())) {
            SysUser applicant = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                    .eq(SysUser::getUserNo, request.getCreateUserNo())
                    .last("LIMIT 1"));
            vo.setApplyByName(applicant != null ? applicant.getUsername() : request.getCreateUserNo());
        }
        enrichInspectionFields(vo, request.getOriginalJudgmentId());

        List<QcRejudgmentRequestVO.ApprovalRecordVO> approvalVOs = approvals.stream().map(a -> {
            QcRejudgmentRequestVO.ApprovalRecordVO r = new QcRejudgmentRequestVO.ApprovalRecordVO();
            r.setApproverNo(a.getApproverNo());
            r.setApprovalAction(a.getApprovalAction());
            r.setDecision(a.getApprovalAction());
            r.setApprovalComment(a.getApprovalComment());
            r.setComment(a.getApprovalComment());
            r.setApprovalTime(a.getApprovalTime());
            r.setApproveTime(a.getApprovalTime());
            return r;
        }).collect(Collectors.toList());
        vo.setApprovalRecords(approvalVOs);
        vo.setApprovalHistory(approvalVOs);

        return vo;
    }

    /**
     * 改判后将原判定依据复制到新判定；若改判为合格，指标依据标记为通过（供质保书等下游使用）。
     */
    private void copyJudgmentEvidences(String originalJudgmentId, String newJudgmentId, String targetJudgmentType) {
        List<QcJudgmentEvidence> sourceList = judgmentEvidenceMapper.selectList(
                new LambdaQueryWrapper<QcJudgmentEvidence>()
                        .eq(QcJudgmentEvidence::getJudgmentId, originalJudgmentId));
        if (sourceList.isEmpty()) {
            return;
        }
        boolean qualifiedOverride = "QUALIFIED".equals(targetJudgmentType);
        for (QcJudgmentEvidence source : sourceList) {
            QcJudgmentEvidence copy = new QcJudgmentEvidence();
            copy.setJudgmentId(newJudgmentId);
            copy.setStandardId(source.getStandardId());
            copy.setIndicatorId(source.getIndicatorId());
            copy.setTestValue(source.getTestValue());
            copy.setUpperLimit(source.getUpperLimit());
            copy.setLowerLimit(source.getLowerLimit());
            copy.setDeviation(source.getDeviation());
            copy.setTriggerRule(source.getTriggerRule());
            copy.setIsPassed(qualifiedOverride ? 1 : source.getIsPassed());
            judgmentEvidenceMapper.insert(copy);
        }
    }

    private void enrichInspectionFields(QcRejudgmentRequestVO vo, String judgmentId) {
        if (!StringUtils.hasText(judgmentId)) {
            return;
        }
        QcJudgmentResult judgment = judgmentResultMapper.selectById(judgmentId);
        if (judgment == null || !StringUtils.hasText(judgment.getRecordId())) {
            return;
        }
        QcInspectionRecord record = inspectionRecordMapper.selectById(judgment.getRecordId());
        if (record != null) {
            vo.setCoilNo(record.getCoilNo());
            vo.setBatchNo(record.getBatchNo());
            vo.setHeatNo(record.getHeatNo());
        }
    }
}
