package com.jhict.quality.service.api;

import com.jhict.quality.dto.AiConfidenceConfigUpdateCmd;
import com.jhict.quality.vo.AiConfidenceConfigVO;

/**
 * AI置信度配置服务。该服务是 qc_ai_confidence_config 表的唯一 Mapper 入口。
 */
public interface AiConfidenceConfigService {

    /**
     * 查询当前生效的 AI 置信度配置。
     *
     * @return 当前生效配置
     */
    AiConfidenceConfigVO getActiveConfig();

    /**
     * 更新当前生效的 AI 置信度配置。
     *
     * @param cmd 更新命令
     * @return 更新后的配置
     */
    AiConfidenceConfigVO updateActiveConfig(AiConfidenceConfigUpdateCmd cmd);
}
