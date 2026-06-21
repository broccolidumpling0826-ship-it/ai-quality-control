package com.jhict.quality.ai.audit;

import com.jhict.quality.ai.client.LlmRequest;
import com.jhict.quality.ai.client.LlmResponse;
import com.jhict.quality.ai.config.AiProperties;
import com.jhict.quality.ai.prompt.PromptRegistry;
import com.jhict.quality.common.util.MaskUtils;
import com.jhict.quality.entity.QcAiCallAuditLog;
import com.jhict.quality.enums.AiErrorType;
import com.jhict.quality.mapper.QcAiCallAuditLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
public class AiAuditService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Resource
    private QcAiCallAuditLogMapper auditLogMapper;

    @Resource
    private AiProperties aiProperties;

    @Resource
    private PromptRegistry promptRegistry;

    public String recordCall(LlmRequest request, LlmResponse response) {
        QcAiCallAuditLog logEntity = new QcAiCallAuditLog();
        logEntity.setCallSource(request.getCallSource().getCode());
        logEntity.setPromptKey(request.getPromptKey());
        logEntity.setPromptVersion(promptRegistry.resolveActiveVersion(request.getPromptKey()));
        logEntity.setModelName(aiProperties.getChatModelName());
        logEntity.setInputSummary(buildInputSummary(request));
        logEntity.setOutputSummary(buildOutputSummary(response));
        logEntity.setCitationIds(request.getCitationIdsJson());
        logEntity.setPromptTokens(response.getPromptTokens());
        logEntity.setCompletionTokens(response.getCompletionTokens());
        logEntity.setTotalTokens(response.getTotalTokens());
        logEntity.setLatencyMs((int) response.getLatencyMs());
        logEntity.setSuccess(response.isSuccess() ? 1 : 0);
        logEntity.setDegraded(response.isDegraded() ? 1 : 0);
        logEntity.setErrorType(response.getErrorType() != null ? response.getErrorType().getCode() : AiErrorType.NONE.getCode());
        logEntity.setTraceId(MDC.get("traceId"));
        logEntity.setBizRefId(request.getBizRefId());
        logEntity.setCreateDateTime(LocalDateTime.now().format(FORMATTER));
        logEntity.setUpdateDateTime(logEntity.getCreateDateTime());
        try {
            auditLogMapper.insert(logEntity);
        } catch (Exception e) {
            log.error("写入 AI 审计日志失败", e);
        }
        return logEntity.getId();
    }

    private String buildInputSummary(LlmRequest request) {
        StringBuilder sb = new StringBuilder();
        if (StringUtils.hasText(request.getSystemPrompt())) {
            sb.append("[system] ").append(MaskUtils.maskSummary(request.getSystemPrompt())).append(" ");
        }
        if (StringUtils.hasText(request.getUserPrompt())) {
            sb.append("[user] ").append(MaskUtils.maskSummary(request.getUserPrompt()));
        }
        return sb.toString().trim();
    }

    private String buildOutputSummary(LlmResponse response) {
        if (!StringUtils.hasText(response.getContent())) {
            return response.getErrorMessage() != null ? MaskUtils.maskSummary(response.getErrorMessage()) : "";
        }
        return MaskUtils.maskSummary(response.getContent());
    }
}
