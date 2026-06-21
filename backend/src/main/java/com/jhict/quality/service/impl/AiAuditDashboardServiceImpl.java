package com.jhict.quality.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jhict.quality.ai.prompt.PromptRegistry;
import com.jhict.quality.common.exception.ServiceException;
import com.jhict.quality.common.util.MaskUtils;
import com.jhict.quality.dto.PromptActivateCmd;
import com.jhict.quality.entity.QcAiCallAuditLog;
import com.jhict.quality.entity.QcAiPromptVersion;
import com.jhict.quality.mapper.QcAiCallAuditLogMapper;
import com.jhict.quality.mapper.QcAiPromptVersionMapper;
import com.jhict.quality.service.api.AiAuditDashboardService;
import com.jhict.quality.vo.AiAuditDashboardVO;
import com.jhict.quality.vo.AiAuditLogVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class AiAuditDashboardServiceImpl implements AiAuditDashboardService {

    private static final String CACHE_KEY = "ai:audit:dashboard";
    private static final long CACHE_TTL_SECONDS = 60L;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Resource
    private QcAiCallAuditLogMapper auditLogMapper;

    @Resource
    private QcAiPromptVersionMapper promptVersionMapper;

    @Resource
    private PromptRegistry promptRegistry;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public AiAuditDashboardVO getDashboard() {
        try {
            String cached = stringRedisTemplate.opsForValue().get(CACHE_KEY);
            if (StringUtils.hasText(cached)) {
                return objectMapper.readValue(cached, AiAuditDashboardVO.class);
            }
        } catch (Exception e) {
            log.warn("读取 AI 审计看板缓存失败", e);
        }

        AiAuditDashboardVO vo = new AiAuditDashboardVO();
        long callCount = auditLogMapper.countAll();
        long totalTokens = auditLogMapper.sumTotalTokens();
        double avgLatency = auditLogMapper.avgLatencyMs();
        long failures = auditLogMapper.countFailures();
        List<Map<String, Object>> bySource = auditLogMapper.countBySource();

        vo.setCallCount(callCount);
        vo.setTotalTokens(totalTokens);
        vo.setAvgLatencyMs(avgLatency);
        vo.setErrorRate(callCount > 0 ? failures * 100.0 / callCount : 0.0);
        vo.setBySource(bySource);
        vo.setCacheUpdatedAt(LocalDateTime.now().format(FORMATTER));

        try {
            stringRedisTemplate.opsForValue().set(CACHE_KEY, objectMapper.writeValueAsString(vo),
                    CACHE_TTL_SECONDS, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("写入 AI 审计看板缓存失败", e);
        }
        return vo;
    }

    @Override
    public AiAuditLogVO replay(String auditLogId) {
        QcAiCallAuditLog logEntity = auditLogMapper.selectById(auditLogId);
        if (logEntity == null) {
            throw new ServiceException("审计日志不存在");
        }
        AiAuditLogVO vo = new AiAuditLogVO();
        vo.setId(logEntity.getId());
        vo.setCallSource(logEntity.getCallSource());
        vo.setPromptKey(logEntity.getPromptKey());
        vo.setPromptVersion(logEntity.getPromptVersion());
        vo.setModelName(logEntity.getModelName());
        vo.setInputSummary(MaskUtils.maskSummary(logEntity.getInputSummary()));
        vo.setOutputSummary(MaskUtils.maskSummary(logEntity.getOutputSummary()));
        vo.setCitationIds(logEntity.getCitationIds());
        vo.setTotalTokens(logEntity.getTotalTokens());
        vo.setLatencyMs(logEntity.getLatencyMs());
        vo.setSuccess(logEntity.getSuccess() != null && logEntity.getSuccess() == 1);
        vo.setDegraded(logEntity.getDegraded() != null && logEntity.getDegraded() == 1);
        vo.setErrorType(logEntity.getErrorType());
        vo.setTraceId(logEntity.getTraceId());
        vo.setCreateDateTime(logEntity.getCreateDateTime());
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void activatePrompt(String promptKey, PromptActivateCmd cmd, String userNo) {
        QcAiPromptVersion target = promptVersionMapper.selectOne(
                new LambdaQueryWrapper<QcAiPromptVersion>()
                        .eq(QcAiPromptVersion::getPromptKey, promptKey)
                        .eq(QcAiPromptVersion::getVersionNo, cmd.getVersionNo())
                        .last("LIMIT 1"));
        if (target == null) {
            throw new ServiceException("Prompt 版本不存在：" + promptKey + " / " + cmd.getVersionNo());
        }
        promptVersionMapper.update(null, new LambdaUpdateWrapper<QcAiPromptVersion>()
                .eq(QcAiPromptVersion::getPromptKey, promptKey)
                .set(QcAiPromptVersion::getIsActive, 0));
        target.setIsActive(1);
        target.setUpdateDateTime(LocalDateTime.now().format(FORMATTER));
        promptVersionMapper.updateById(target);
        promptRegistry.getActiveTemplate(promptKey);
    }
}
