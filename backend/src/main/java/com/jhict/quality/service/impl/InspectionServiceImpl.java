package com.jhict.quality.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jhict.quality.common.annotation.AuditLog;
import com.jhict.quality.common.entity.ApiResult;
import com.jhict.quality.common.exception.ServiceException;
import com.jhict.quality.dto.QcInspectionRecordAddCmd;
import com.jhict.quality.dto.QcInspectionRecordPageQuery;
import com.jhict.quality.engine.JudgmentEngine;
import com.jhict.quality.engine.model.JudgmentInput;
import com.jhict.quality.engine.model.JudgmentOutput;
import com.jhict.quality.entity.QcInspectionRecord;
import com.jhict.quality.entity.QcInspectionValue;
import com.jhict.quality.entity.QcJudgmentResult;
import com.jhict.quality.mapper.QcInspectionRecordMapper;
import com.jhict.quality.mapper.QcInspectionValueMapper;
import com.jhict.quality.service.api.InspectionService;
import com.jhict.quality.service.api.JudgmentService;
import com.jhict.quality.vo.InspectionResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class InspectionServiceImpl implements InspectionService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String DEFAULT_COMPANY_ID = "DEFAULT";

    @Resource
    private QcInspectionRecordMapper inspectionRecordMapper;

    @Resource
    private QcInspectionValueMapper inspectionValueMapper;

    @Resource
    private JudgmentEngine judgmentEngine;

    @Resource
    private JudgmentService judgmentService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InspectionResultVO addRecord(QcInspectionRecordAddCmd cmd) {
        // Step 1: 解析检验时间
        LocalDateTime testTime;
        try {
            testTime = LocalDateTime.parse(cmd.getTestTime(), FORMATTER);
        } catch (Exception e) {
            throw new ServiceException(ApiResult.CODE_BAD_REQUEST, "检验时间格式错误，请使用 yyyy-MM-dd HH:mm:ss");
        }

        // Step 2: 保存InspectionRecord（batchNo = heatNo）
        QcInspectionRecord record = new QcInspectionRecord();
        record.setHeatNo(cmd.getHeatNo());
        record.setCoilNo(cmd.getCoilNo());
        record.setBatchNo(cmd.getHeatNo());  // batchNo = heatNo
        record.setSampleType(cmd.getSampleType());
        record.setTestTime(testTime);
        record.setTesterNo(cmd.getTesterNo());
        record.setCustomerId(cmd.getCustomerId());
        record.setProductVariety(cmd.getProductVariety());
        record.setProductGrade(cmd.getProductGrade());
        record.setProductSpec(cmd.getProductSpec());
        record.setStatus("NORMAL");
        inspectionRecordMapper.insert(record);

        String recordId = record.getId();

        // Step 3: 批量保存InspectionValue
        List<QcInspectionValue> valueList = new ArrayList<>();
        for (QcInspectionRecordAddCmd.InspectionValueItem item : cmd.getValues()) {
            QcInspectionValue value = new QcInspectionValue();
            value.setRecordId(recordId);
            value.setIndicatorId(item.getIndicatorId());
            value.setTestValue(item.getTestValue());
            value.setValueText(item.getValueText());
            valueList.add(value);
        }
        for (QcInspectionValue value : valueList) {
            inspectionValueMapper.insert(value);
        }

        // Step 4: 构建JudgmentInput，调JudgmentEngine.judge()
        List<JudgmentInput.InspectionValueItem> engineValues = cmd.getValues().stream()
                .map(v -> JudgmentInput.InspectionValueItem.builder()
                        .indicatorId(v.getIndicatorId())
                        .testValue(v.getTestValue())
                        .valueText(v.getValueText())
                        .build())
                .collect(Collectors.toList());

        JudgmentInput judgmentInput = JudgmentInput.builder()
                .recordId(recordId)
                .customerId(cmd.getCustomerId())
                .productVariety(cmd.getProductVariety())
                .productGrade(cmd.getProductGrade())
                .productSpec(cmd.getProductSpec())
                .testTime(testTime)
                .values(engineValues)
                .build();

        JudgmentOutput judgmentOutput = judgmentEngine.judge(judgmentInput);

        // Step 5: 调JudgmentService.saveJudgmentResult()保存结论
        QcJudgmentResult judgmentResult = judgmentService.saveJudgmentResult(recordId, judgmentOutput);

        // Step 6: DEL Redis看板缓存
        try {
            stringRedisTemplate.delete("dashboard:summary:" + DEFAULT_COMPANY_ID);
        } catch (Exception e) {
            log.warn("删除看板缓存失败，不影响业务，error={}", e.getMessage());
        }

        // Step 7: 构建并返回InspectionResultVO
        InspectionResultVO resultVO = new InspectionResultVO();
        resultVO.setRecordId(recordId);
        resultVO.setJudgmentId(judgmentResult.getId());
        resultVO.setJudgmentType(judgmentResult.getJudgmentType());
        resultVO.setJudgmentTime(judgmentResult.getJudgmentTime().format(FORMATTER));

        log.info("新增检验记录成功，recordId={}, judgmentType={}",
                recordId, judgmentResult.getJudgmentType());
        return resultVO;
    }

    @Override
    @AuditLog(operationType = "VOID_INSPECTION", targetEntity = "QcInspectionRecord")
    @Transactional(rollbackFor = Exception.class)
    public void voidRecord(String id, String reason) {
        // Step 1: 查记录
        QcInspectionRecord record = inspectionRecordMapper.selectById(id);
        if (record == null) {
            throw new ServiceException(ApiResult.CODE_NOT_FOUND, "检验记录不存在");
        }

        // Step 2: 校验状态必须为NORMAL
        if (!"NORMAL".equals(record.getStatus())) {
            throw new ServiceException(ApiResult.CODE_BAD_REQUEST, "仅正常状态的记录可以作废，当前状态：" + record.getStatus());
        }

        if (!StringUtils.hasText(reason)) {
            throw new ServiceException(ApiResult.CODE_BAD_REQUEST, "作废原因不能为空");
        }

        // Step 3: 校验权限（当前用户工号=testerNo 或 Sa-Token角色包含QUALITY_SUPERVISOR）
        String currentUserNo = getCurrentUserNo();
        boolean isTester = record.getTesterNo().equals(currentUserNo);
        boolean isSupervisor = hasRole("QUALITY_SUPERVISOR");
        if (!isTester && !isSupervisor) {
            throw new ServiceException(ApiResult.CODE_FORBIDDEN, "无权作废该检验记录，仅检验人本人或质量主管可操作");
        }

        // Step 4: 软作废
        record.setStatus("VOID");
        record.setVoidReason(reason);
        record.setVoidBy(currentUserNo);
        record.setVoidTime(LocalDateTime.now());
        inspectionRecordMapper.updateById(record);

        // Step 5: 关联最终判定结论 isFinal=0（通过JudgmentResultMapper直接更新）
        // 注入judgmentResultMapper通过jdbcTemplate实现，此处通过JudgmentService接口路由
        // 使用直接Mapper操作，避免循环依赖
        // 这里手动更新 qc_judgment_result.is_final=0
        try {
            // 通过动态SQL更新isFinal
            com.jhict.quality.mapper.QcJudgmentResultMapper judgmentResultMapper =
                    getJudgmentResultMapper();
            if (judgmentResultMapper != null) {
                judgmentResultMapper.invalidateByRecordId(id);
            }
        } catch (Exception e) {
            log.warn("更新判定结论isFinal失败，recordId={}，error={}", id, e.getMessage());
        }

        // Step 6: 删除看板缓存
        try {
            stringRedisTemplate.delete("dashboard:summary:" + DEFAULT_COMPANY_ID);
        } catch (Exception e) {
            log.warn("删除看板缓存失败，error={}", e.getMessage());
        }

        log.info("作废检验记录，id={}, voidBy={}", id, currentUserNo);
    }

    @Override
    public IPage<QcInspectionRecord> page(QcInspectionRecordPageQuery query) {
        Page<QcInspectionRecord> pageParam = new Page<>(query.getPageNum(), query.getPageSize());

        LambdaQueryWrapper<QcInspectionRecord> wrapper = new LambdaQueryWrapper<QcInspectionRecord>()
                .like(StringUtils.hasText(query.getCoilNo()), QcInspectionRecord::getCoilNo, query.getCoilNo())
                .like(StringUtils.hasText(query.getHeatNo()), QcInspectionRecord::getHeatNo, query.getHeatNo())
                .eq(StringUtils.hasText(query.getStatus()), QcInspectionRecord::getStatus, query.getStatus())
                .eq(StringUtils.hasText(query.getSampleType()), QcInspectionRecord::getSampleType, query.getSampleType())
                .ge(StringUtils.hasText(query.getTestTimeStart()),
                        QcInspectionRecord::getTestTime,
                        StringUtils.hasText(query.getTestTimeStart())
                                ? LocalDateTime.parse(query.getTestTimeStart(), FORMATTER) : null)
                .le(StringUtils.hasText(query.getTestTimeEnd()),
                        QcInspectionRecord::getTestTime,
                        StringUtils.hasText(query.getTestTimeEnd())
                                ? LocalDateTime.parse(query.getTestTimeEnd(), FORMATTER) : null)
                .orderByDesc(QcInspectionRecord::getTestTime);

        return inspectionRecordMapper.selectPage(pageParam, wrapper);
    }

    @Override
    public Map<String, Object> getById(String id) {
        QcInspectionRecord record = inspectionRecordMapper.selectById(id);
        if (record == null) {
            throw new ServiceException(ApiResult.CODE_NOT_FOUND, "检验记录不存在");
        }

        List<QcInspectionValue> values = inspectionValueMapper.findByRecordId(id);

        Map<String, Object> result = new HashMap<>();
        result.put("record", record);
        result.put("values", values);
        return result;
    }

    private String getCurrentUserNo() {
        try {
            Object loginId = StpUtil.getLoginIdDefaultNull();
            return loginId != null ? loginId.toString() : "SYSTEM";
        } catch (Exception e) {
            return "SYSTEM";
        }
    }

    private boolean hasRole(String role) {
        try {
            return StpUtil.hasRole(role);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 通过Spring应用上下文获取JudgmentResultMapper（避免构造循环依赖）
     */
    @Resource
    private com.jhict.quality.mapper.QcJudgmentResultMapper qcJudgmentResultMapper;

    private com.jhict.quality.mapper.QcJudgmentResultMapper getJudgmentResultMapper() {
        return qcJudgmentResultMapper;
    }
}
