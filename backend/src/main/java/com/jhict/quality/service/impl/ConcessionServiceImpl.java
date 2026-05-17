package com.jhict.quality.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jhict.quality.common.annotation.AuditLog;
import com.jhict.quality.common.exception.ServiceException;
import com.jhict.quality.common.util.AuthUtils;
import com.jhict.quality.dto.ConcessionConfirmCmd;
import com.jhict.quality.dto.QcConcessionAddCmd;
import com.jhict.quality.dto.QcConcessionPageQuery;
import com.jhict.quality.enums.JudgmentType;
import com.jhict.quality.entity.QcConcessionAcceptance;
import com.jhict.quality.entity.QcInspectionRecord;
import com.jhict.quality.entity.QcJudgmentResult;
import com.jhict.quality.entity.SysUser;
import com.jhict.quality.mapper.QcConcessionAcceptanceMapper;
import com.jhict.quality.mapper.QcInspectionRecordMapper;
import com.jhict.quality.mapper.QcJudgmentResultMapper;
import com.jhict.quality.mapper.SysUserMapper;
import com.jhict.quality.service.api.ConcessionService;
import com.jhict.quality.service.api.FileStorageService;
import com.jhict.quality.service.api.NotificationService;
import com.jhict.quality.vo.QcConcessionVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ConcessionServiceImpl implements ConcessionService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Resource
    private QcConcessionAcceptanceMapper concessionMapper;

    @Resource
    private QcJudgmentResultMapper judgmentResultMapper;

    @Resource
    private QcInspectionRecordMapper inspectionRecordMapper;

    @Resource
    private SysUserMapper sysUserMapper;

    @Resource
    private NotificationService notificationService;

    @Resource
    private FileStorageService fileStorageService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String apply(QcConcessionAddCmd cmd) {
        // 校验判定结论存在且为 CAN_CONCESSION
        QcJudgmentResult judgment = judgmentResultMapper.selectById(cmd.getJudgmentId());
        if (judgment == null) {
            throw new ServiceException("判定结论不存在，id=" + cmd.getJudgmentId());
        }
        if (!JudgmentType.CAN_CONCESSION.getCode().equals(judgment.getJudgmentType())) {
            throw new ServiceException("只有判定结论为【可让步】的记录才能发起让步接收申请，当前判定：" + judgment.getJudgmentType());
        }

        QcConcessionAcceptance acceptance = new QcConcessionAcceptance();
        acceptance.setJudgmentId(cmd.getJudgmentId());
        acceptance.setConcessionScope(cmd.getConcessionScope());
        acceptance.setRiskDescription(cmd.getRiskDescription());
        acceptance.setEffectiveDate(cmd.getEffectiveDate());
        acceptance.setExpiryDate(cmd.getExpiryDate());
        acceptance.setConfirmStatus("PENDING");
        acceptance.setApprovalStatus("PENDING");
        concessionMapper.insert(acceptance);

        log.info("让步接收申请创建成功，id={}，judgmentId={}", acceptance.getId(), cmd.getJudgmentId());
        return acceptance.getId();
    }

    @Override
    @AuditLog(operationType = "CONFIRM_CONCESSION", targetEntity = "QcConcessionAcceptance")
    @Transactional(rollbackFor = Exception.class)
    public void confirm(String id, ConcessionConfirmCmd cmd, MultipartFile file) {
        QcConcessionAcceptance acceptance = requireExists(id);

        // 附件不可替换
        if (StringUtils.hasText(acceptance.getConfirmAttachmentUrl())) {
            throw new ServiceException("客户确认附件已上传，如需更新请作废本申请重新发起");
        }

        // 强制附件
        if (file == null || file.isEmpty()) {
            throw new ServiceException("客户确认附件不能为空");
        }

        String attachmentUrl = fileStorageService.save(file, "concession-confirm");

        acceptance.setConfirmAttachmentUrl(attachmentUrl);
        acceptance.setConfirmNote(cmd != null ? cmd.getConfirmNote() : null);
        acceptance.setConfirmStatus("CONFIRMED");
        acceptance.setConfirmUploaderNo(getLoginUserNo());
        acceptance.setConfirmUploadTime(LocalDateTime.now());
        concessionMapper.updateById(acceptance);

        log.info("让步接收客户确认成功，id={}，uploadedBy={}", id, acceptance.getConfirmUploaderNo());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reject(String id, String rejectNote, MultipartFile rejectFile) {
        QcConcessionAcceptance acceptance = requireExists(id);

        if (rejectFile != null && !rejectFile.isEmpty()) {
            String rejectAttachment = fileStorageService.save(rejectFile, "concession-reject");
            acceptance.setConfirmAttachmentUrl(rejectAttachment);
        }
        acceptance.setConfirmNote(rejectNote);
        acceptance.setConfirmStatus("REJECTED");
        acceptance.setApprovalStatus("REJECTED");
        concessionMapper.updateById(acceptance);

        log.info("让步接收拒绝成功，id={}", id);
    }

    @Override
    @AuditLog(operationType = "APPROVE_CONCESSION", targetEntity = "QcConcessionAcceptance")
    @Transactional(rollbackFor = Exception.class)
    public void approve(String id, String comment, String operatorRole) {
        QcConcessionAcceptance acceptance = requireExists(id);

        String currentApprovalStatus = acceptance.getApprovalStatus();

        if (AuthUtils.isAdmin()) {
            // 系统管理员：跳过角色与前置状态校验，可直接推进或终审
            if ("PENDING_APPROVAL".equals(currentApprovalStatus)) {
                acceptance.setApprovalStatus("SALES_APPROVED");
            } else if ("SALES_APPROVED".equals(currentApprovalStatus)) {
                acceptance.setApprovalStatus("APPROVED");
            } else if (!"APPROVED".equals(currentApprovalStatus) && !"REJECTED".equals(currentApprovalStatus)) {
                acceptance.setApprovalStatus("APPROVED");
            }
            log.info("让步接收管理员审批通过，id={}", id);
        } else if ("SALES_MANAGER".equals(operatorRole)) {
            // 销售经理：只能在 PENDING_APPROVAL 状态下做第一次审批
            if (!"PENDING_APPROVAL".equals(currentApprovalStatus)) {
                throw new ServiceException("销售经理只能审批状态为【待审批】的申请，当前状态：" + currentApprovalStatus);
            }
            acceptance.setApprovalStatus("SALES_APPROVED");
            log.info("让步接收销售审批通过，id={}", id);
        } else if ("QUALITY_MANAGER".equals(operatorRole)) {
            // 质量经理：只能在 SALES_APPROVED 状态下做最终审批，且必须已客户确认
            if (!"SALES_APPROVED".equals(currentApprovalStatus)) {
                throw new ServiceException("质量经理只能审批状态为【销售已审批】的申请，当前状态：" + currentApprovalStatus);
            }
            if (!"CONFIRMED".equals(acceptance.getConfirmStatus())) {
                throw new ServiceException("让步接收尚未获得客户确认，不可审批通过，当前确认状态：" + acceptance.getConfirmStatus());
            }
            acceptance.setApprovalStatus("APPROVED");
            log.info("让步接收质量审批通过，id={}", id);
        } else {
            throw new ServiceException("当前角色无审批权限，需要 SALES_MANAGER 或 QUALITY_MANAGER 角色，当前角色：" + operatorRole);
        }

        if (StringUtils.hasText(comment)) {
            acceptance.setConfirmNote(
                    StringUtils.hasText(acceptance.getConfirmNote())
                            ? acceptance.getConfirmNote() + "；审批意见：" + comment
                            : "审批意见：" + comment
            );
        }
        concessionMapper.updateById(acceptance);
    }

    @Override
    public IPage<QcConcessionVO> page(QcConcessionPageQuery query) {
        int pageNum = query.getPageNum() != null && query.getPageNum() > 0 ? query.getPageNum() : 1;
        int pageSize = query.getPageSize() != null && query.getPageSize() > 0 ? query.getPageSize() : 10;

        LambdaQueryWrapper<QcConcessionAcceptance> wrapper = new LambdaQueryWrapper<QcConcessionAcceptance>()
                .orderByDesc(QcConcessionAcceptance::getCreateDateTime);
        if (StringUtils.hasText(query.getConfirmStatus())) {
            wrapper.eq(QcConcessionAcceptance::getConfirmStatus, query.getConfirmStatus());
        }
        if (StringUtils.hasText(query.getApprovalStatus())) {
            wrapper.eq(QcConcessionAcceptance::getApprovalStatus, query.getApprovalStatus());
        }
        if (StringUtils.hasText(query.getCoilNo())) {
            Set<String> judgmentIds = resolveJudgmentIdsByCoilNo(query.getCoilNo());
            if (judgmentIds.isEmpty()) {
                Page<QcConcessionVO> empty = new Page<>(pageNum, pageSize, 0);
                empty.setRecords(Collections.emptyList());
                return empty;
            }
            wrapper.in(QcConcessionAcceptance::getJudgmentId, judgmentIds);
        }

        IPage<QcConcessionAcceptance> pageResult = concessionMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);

        List<QcConcessionVO> voList = pageResult.getRecords().stream()
                .map(this::toVO)
                .collect(Collectors.toList());

        Page<QcConcessionVO> voPage = new Page<>(pageResult.getCurrent(), pageResult.getSize(), pageResult.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public QcConcessionVO getById(String id) {
        return toVO(requireExists(id));
    }

    // ---------- private helpers ----------

    private QcConcessionAcceptance requireExists(String id) {
        QcConcessionAcceptance acceptance = concessionMapper.selectById(id);
        if (acceptance == null) {
            throw new ServiceException("让步接收申请不存在，id=" + id);
        }
        return acceptance;
    }

    private QcConcessionVO toVO(QcConcessionAcceptance acceptance) {
        QcConcessionVO vo = new QcConcessionVO();
        vo.setId(acceptance.getId());
        vo.setJudgmentId(acceptance.getJudgmentId());
        vo.setConcessionScope(acceptance.getConcessionScope());
        vo.setRiskDescription(acceptance.getRiskDescription());
        vo.setEffectiveDate(acceptance.getEffectiveDate());
        vo.setExpiryDate(acceptance.getExpiryDate());
        vo.setValidFrom(acceptance.getEffectiveDate());
        vo.setValidTo(acceptance.getExpiryDate());
        vo.setConfirmStatus(acceptance.getConfirmStatus());
        vo.setConfirmAttachmentUrl(acceptance.getConfirmAttachmentUrl());
        vo.setConfirmFileUrl(acceptance.getConfirmAttachmentUrl());
        vo.setConfirmNote(acceptance.getConfirmNote());
        vo.setConfirmUploaderNo(acceptance.getConfirmUploaderNo());
        if (acceptance.getConfirmUploadTime() != null) {
            vo.setConfirmUploadTime(acceptance.getConfirmUploadTime().format(DATE_TIME_FORMATTER));
        }
        if (StringUtils.hasText(acceptance.getConfirmUploaderNo())) {
            SysUser uploader = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                    .eq(SysUser::getUserNo, acceptance.getConfirmUploaderNo())
                    .last("LIMIT 1"));
            vo.setConfirmUploadByName(
                    uploader != null ? uploader.getUsername() : acceptance.getConfirmUploaderNo());
        }
        if (StringUtils.hasText(acceptance.getConfirmAttachmentUrl())) {
            vo.setConfirmFileName(extractDisplayFileName(acceptance.getConfirmAttachmentUrl()));
        }
        vo.setApprovalStatus(acceptance.getApprovalStatus());
        vo.setConcessionStatus(resolveConcessionStatus(
                acceptance.getConfirmStatus(), acceptance.getApprovalStatus()));

        int remaining = 0;
        if (acceptance.getExpiryDate() != null) {
            remaining = (int) ChronoUnit.DAYS.between(LocalDate.now(), acceptance.getExpiryDate());
        }
        vo.setRemainingDays(remaining);
        vo.setReason(acceptance.getRiskDescription());
        vo.setApplyTime(acceptance.getCreateDateTime());
        vo.setApplyBy(acceptance.getCreateUserNo());
        if (StringUtils.hasText(acceptance.getCreateUserNo())) {
            SysUser applicant = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                    .eq(SysUser::getUserNo, acceptance.getCreateUserNo())
                    .last("LIMIT 1"));
            vo.setApplyByName(applicant != null ? applicant.getUsername() : acceptance.getCreateUserNo());
        }
        enrichInspectionFields(vo, acceptance.getJudgmentId());
        return vo;
    }

    private String resolveConcessionStatus(String confirmStatus, String approvalStatus) {
        if ("APPROVED".equals(approvalStatus)) {
            return "APPROVED";
        }
        if ("REJECTED".equals(approvalStatus) || "INVALID".equals(approvalStatus)) {
            return approvalStatus;
        }
        if ("CONFIRMED".equals(confirmStatus)) {
            return "PENDING_APPROVAL";
        }
        if (StringUtils.hasText(approvalStatus) && !"PENDING".equals(approvalStatus)) {
            return approvalStatus;
        }
        return null;
    }

    private String extractDisplayFileName(String relativePath) {
        String name = relativePath.substring(relativePath.lastIndexOf('/') + 1);
        int underscoreIdx = name.indexOf('_');
        if (underscoreIdx >= 0 && underscoreIdx < name.length() - 1) {
            return name.substring(underscoreIdx + 1);
        }
        return name;
    }

    private void enrichInspectionFields(QcConcessionVO vo, String judgmentId) {
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
        }
    }

    private Set<String> resolveJudgmentIdsByCoilNo(String coilNo) {
        List<QcInspectionRecord> records = inspectionRecordMapper.selectList(
                new LambdaQueryWrapper<QcInspectionRecord>().like(QcInspectionRecord::getCoilNo, coilNo));
        if (records.isEmpty()) {
            return Collections.emptySet();
        }
        Set<String> recordIds = records.stream().map(QcInspectionRecord::getId).collect(Collectors.toSet());
        return judgmentResultMapper.selectList(
                        new LambdaQueryWrapper<QcJudgmentResult>().in(QcJudgmentResult::getRecordId, recordIds))
                .stream()
                .map(QcJudgmentResult::getId)
                .collect(Collectors.toSet());
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
