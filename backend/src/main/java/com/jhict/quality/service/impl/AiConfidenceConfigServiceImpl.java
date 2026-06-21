package com.jhict.quality.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jhict.quality.common.annotation.AuditLog;
import com.jhict.quality.common.entity.ApiResult;
import com.jhict.quality.common.exception.ServiceException;
import com.jhict.quality.dto.AiConfidenceConfigUpdateCmd;
import com.jhict.quality.entity.QcAiConfidenceConfig;
import com.jhict.quality.mapper.QcAiConfidenceConfigMapper;
import com.jhict.quality.service.api.AiConfidenceConfigService;
import com.jhict.quality.vo.AiConfidenceConfigVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class AiConfidenceConfigServiceImpl implements AiConfidenceConfigService {

    private static final BigDecimal ONE = new BigDecimal("1.0000");

    @Resource
    private QcAiConfidenceConfigMapper configMapper;

    @Override
    public AiConfidenceConfigVO getActiveConfig() {
        return toVO(getRequiredActiveConfig());
    }

    @Override
    @AuditLog(operationType = "UPDATE_AI_CONFIDENCE_CONFIG", targetEntity = "QcAiConfidenceConfig")
    @Transactional(rollbackFor = Exception.class)
    public AiConfidenceConfigVO updateActiveConfig(AiConfidenceConfigUpdateCmd cmd) {
        validateConfig(cmd);
        QcAiConfidenceConfig config = getRequiredActiveConfig();
        config.setConfigName(cmd.getConfigName());
        config.setRuleWeight(cmd.getRuleWeight());
        config.setRagWeight(cmd.getRagWeight());
        config.setLlmWeight(cmd.getLlmWeight());
        config.setHighThreshold(cmd.getHighThreshold());
        config.setMediumThreshold(cmd.getMediumThreshold());
        config.setLowThreshold(cmd.getLowThreshold());
        config.setEnabled(cmd.getEnabled() == null ? 1 : cmd.getEnabled());
        config.setUpdatedBy(currentLoginId());
        config.setUpdatedAt(LocalDateTime.now());
        config.setRemark(cmd.getRemark());
        configMapper.updateById(config);
        return toVO(config);
    }

    private QcAiConfidenceConfig getRequiredActiveConfig() {
        QcAiConfidenceConfig config = configMapper.selectOne(new LambdaQueryWrapper<QcAiConfidenceConfig>()
                .eq(QcAiConfidenceConfig::getActiveFlag, 1)
                .orderByDesc(QcAiConfidenceConfig::getUpdatedAt)
                .last("LIMIT 1"));
        if (config == null) {
            throw new ServiceException(ApiResult.CODE_NOT_FOUND, "AI置信度配置不存在，请先执行初始化脚本");
        }
        return config;
    }

    private void validateConfig(AiConfidenceConfigUpdateCmd cmd) {
        BigDecimal total = cmd.getRuleWeight().add(cmd.getRagWeight()).add(cmd.getLlmWeight());
        if (total.compareTo(ONE) != 0) {
            throw new ServiceException(ApiResult.CODE_BAD_REQUEST, "规则/RAG/LLM 权重合计必须等于 1.0000");
        }
        if (cmd.getHighThreshold().compareTo(cmd.getMediumThreshold()) <= 0) {
            throw new ServiceException(ApiResult.CODE_BAD_REQUEST, "高置信阈值必须大于中置信阈值");
        }
        if (cmd.getMediumThreshold().compareTo(cmd.getLowThreshold()) <= 0) {
            throw new ServiceException(ApiResult.CODE_BAD_REQUEST, "中置信阈值必须大于低置信阈值");
        }
        if (cmd.getEnabled() != null && cmd.getEnabled() != 0 && cmd.getEnabled() != 1) {
            throw new ServiceException(ApiResult.CODE_BAD_REQUEST, "启用状态仅支持 0/1");
        }
    }

    private AiConfidenceConfigVO toVO(QcAiConfidenceConfig config) {
        AiConfidenceConfigVO vo = new AiConfidenceConfigVO();
        vo.setId(config.getId());
        vo.setConfigName(config.getConfigName());
        vo.setRuleWeight(config.getRuleWeight());
        vo.setRagWeight(config.getRagWeight());
        vo.setLlmWeight(config.getLlmWeight());
        vo.setHighThreshold(config.getHighThreshold());
        vo.setMediumThreshold(config.getMediumThreshold());
        vo.setLowThreshold(config.getLowThreshold());
        vo.setEnabled(config.getEnabled());
        vo.setActiveFlag(config.getActiveFlag());
        vo.setUpdatedBy(config.getUpdatedBy());
        vo.setUpdatedAt(config.getUpdatedAt());
        vo.setRemark(config.getRemark());
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
