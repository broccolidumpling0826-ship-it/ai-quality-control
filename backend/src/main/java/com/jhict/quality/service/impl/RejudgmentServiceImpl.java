package com.jhict.quality.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jhict.quality.common.annotation.AuditLog;
import com.jhict.quality.common.exception.ServiceException;
import com.jhict.quality.dto.QcRejudgmentRequestAddCmd;
import com.jhict.quality.dto.RejudgmentApproveCmd;
import com.jhict.quality.entity.QcConcessionAcceptance;
import com.jhict.quality.entity.QcJudgmentResult;
import com.jhict.quality.entity.QcRejudgmentApproval;
import com.jhict.quality.entity.QcRejudgmentRequest;
import com.jhict.quality.mapper.QcConcessionAcceptanceMapper;
import com.jhict.quality.mapper.QcJudgmentResultMapper;
import com.jhict.quality.mapper.QcRejudgmentApprovalMapper;
import com.jhict.quality.mapper.QcRejudgmentRequestMapper;
import com.jhict.quality.service.api.NotificationService;
import com.jhict.quality.service.api.RejudgmentService;
import com.jhict.quality.vo.QcRejudgmentRequestVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
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

        // 2. 校验审批人角色（ENHANCED 需要 QUALITY_MANAGER）
        if ("ENHANCED".equals(request.getApprovalLevel())) {
            Object role = StpUtil.getSession().get("role");
            if (!"QUALITY_MANAGER".equals(role)) {
                throw new ServiceException("逆向改判（加强审批）需要质量经理权限");
            }
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
    public IPage<QcRejudgmentRequest> page(int pageNum, int pageSize, String approvalStatus, Integer isReverse) {
        LambdaQueryWrapper<QcRejudgmentRequest> wrapper = new LambdaQueryWrapper<QcRejudgmentRequest>()
                .orderByDesc(QcRejudgmentRequest::getCreateDateTime);
        if (StringUtils.hasText(approvalStatus)) {
            wrapper.eq(QcRejudgmentRequest::getApprovalStatus, approvalStatus);
        }
        if (isReverse != null) {
            wrapper.eq(QcRejudgmentRequest::getIsReverse, isReverse);
        }
        return rejudgmentRequestMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
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
        vo.setAffectScope(request.getAffectScope());
        vo.setIsReverse(request.getIsReverse());
        vo.setApprovalLevel(request.getApprovalLevel());
        vo.setApprovalStatus(request.getApprovalStatus());
        vo.setIsReverseLabel(Integer.valueOf(1).equals(request.getIsReverse()) ? "逆向改判" : "");

        List<QcRejudgmentRequestVO.ApprovalRecordVO> approvalVOs = approvals.stream().map(a -> {
            QcRejudgmentRequestVO.ApprovalRecordVO r = new QcRejudgmentRequestVO.ApprovalRecordVO();
            r.setApproverNo(a.getApproverNo());
            r.setApprovalAction(a.getApprovalAction());
            r.setApprovalComment(a.getApprovalComment());
            r.setApprovalTime(a.getApprovalTime());
            return r;
        }).collect(Collectors.toList());
        vo.setApprovalRecords(approvalVOs);

        return vo;
    }
}
