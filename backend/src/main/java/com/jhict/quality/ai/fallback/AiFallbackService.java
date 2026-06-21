package com.jhict.quality.ai.fallback;

import com.jhict.quality.ai.client.LlmResponse;
import com.jhict.quality.enums.AiErrorType;
import org.springframework.stereotype.Service;

/**
 * AI 降级兜底响应
 */
@Service
public class AiFallbackService {

    public LlmResponse buildDisabledResponse(long latencyMs) {
        return LlmResponse.builder()
                .success(false)
                .degraded(true)
                .content("AI 服务未启用，已切换为规则引擎摘要模式。")
                .errorType(AiErrorType.DISABLED)
                .errorMessage("AI disabled")
                .latencyMs(latencyMs)
                .build();
    }

    public LlmResponse buildTimeoutResponse(long latencyMs) {
        return LlmResponse.builder()
                .success(false)
                .degraded(true)
                .content("AI 服务响应超时，已切换为规则引擎摘要模式。")
                .errorType(AiErrorType.TIMEOUT)
                .errorMessage("timeout")
                .latencyMs(latencyMs)
                .build();
    }

    public LlmResponse buildRateLimitResponse(long latencyMs) {
        return LlmResponse.builder()
                .success(false)
                .degraded(true)
                .content("AI 服务繁忙（限流），请稍后重试或使用规则摘要。")
                .errorType(AiErrorType.RATE_LIMIT)
                .errorMessage("429")
                .latencyMs(latencyMs)
                .build();
    }

    public LlmResponse buildUnavailableResponse(long latencyMs) {
        return LlmResponse.builder()
                .success(false)
                .degraded(true)
                .content("AI 服务暂不可用，已切换为规则引擎摘要模式。")
                .errorType(AiErrorType.UNAVAILABLE)
                .errorMessage("unavailable")
                .latencyMs(latencyMs)
                .build();
    }

    public LlmResponse buildInjectionBlockedResponse(long latencyMs) {
        return LlmResponse.builder()
                .success(false)
                .degraded(false)
                .content("检测到不安全输入，请求已拒绝。")
                .errorType(AiErrorType.INJECTION_BLOCKED)
                .errorMessage("injection blocked")
                .latencyMs(latencyMs)
                .build();
    }
}
