package com.jhict.quality.ai.client;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.jhict.quality.ai.audit.AiAuditService;
import com.jhict.quality.ai.config.AiProperties;
import com.jhict.quality.ai.fallback.AiFallbackService;
import com.jhict.quality.ai.prompt.PromptSanitizer;
import com.jhict.quality.enums.AiErrorType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class LlmClient {

    @Resource
    private AiProperties aiProperties;

    @Resource
    private RestTemplate aiRestTemplate;

    @Resource
    private AiAuditService aiAuditService;

    @Resource
    private AiFallbackService aiFallbackService;

    @Resource
    private PromptSanitizer promptSanitizer;

    private final AtomicInteger consecutiveFailures = new AtomicInteger(0);

    private volatile long circuitOpenUntilMs = 0L;

    public LlmResponse chat(LlmRequest request) {
        long start = System.currentTimeMillis();
        LlmResponse response = doChatInternal(request, start);
        String auditId = aiAuditService.recordCall(request, response);
        response.setAuditLogId(auditId);
        return response;
    }

    private LlmResponse doChatInternal(LlmRequest request, long startMs) {
        if (!aiProperties.isEnabled()) {
            return aiFallbackService.buildDisabledResponse(elapsed(startMs));
        }
        if (isCircuitOpen()) {
            return aiFallbackService.buildUnavailableResponse(elapsed(startMs));
        }
        if (promptSanitizer.containsInjection(request.getUserPrompt())) {
            return aiFallbackService.buildInjectionBlockedResponse(elapsed(startMs));
        }
        LlmResponse mockResponse = tryMockFailure(startMs);
        if (mockResponse != null) {
            return mockResponse;
        }
        return invokeWithRetry(request, startMs, 0);
    }

    private LlmResponse invokeWithRetry(LlmRequest request, long startMs, int attempt) {
        try {
            LlmResponse ok = callRemoteApi(request, startMs);
            consecutiveFailures.set(0);
            return ok;
        } catch (HttpStatusCodeException ex) {
            if (ex.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS
                    && attempt < aiProperties.getMaxRetriesOn429()) {
                sleepQuietly(1000L);
                return invokeWithRetry(request, startMs, attempt + 1);
            }
            markFailure();
            if (ex.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                return aiFallbackService.buildRateLimitResponse(elapsed(startMs));
            }
            return aiFallbackService.buildUnavailableResponse(elapsed(startMs));
        } catch (ResourceAccessException ex) {
            markFailure();
            if (ex.getMessage() != null && ex.getMessage().toLowerCase().contains("timed out")) {
                return aiFallbackService.buildTimeoutResponse(elapsed(startMs));
            }
            return aiFallbackService.buildUnavailableResponse(elapsed(startMs));
        } catch (Exception ex) {
            markFailure();
            log.error("LLM 调用异常", ex);
            return aiFallbackService.buildUnavailableResponse(elapsed(startMs));
        }
    }

    private LlmResponse callRemoteApi(LlmRequest request, long startMs) {
        if (!StringUtils.hasText(aiProperties.getApiKey())) {
            return aiFallbackService.buildDisabledResponse(elapsed(startMs));
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(aiProperties.getApiKey());

        Map<String, Object> body = new HashMap<>();
        body.put("model", aiProperties.getModel());
        JSONArray messages = new JSONArray();
        if (StringUtils.hasText(request.getSystemPrompt())) {
            messages.add(message("system", request.getSystemPrompt()));
        }
        messages.add(message("user", promptSanitizer.wrapUserContent(request.getUserPrompt())));
        body.put("messages", messages);

        String url = trimTrailingSlash(aiProperties.getBaseUrl()) + "/chat/completions";
        ResponseEntity<String> entity = aiRestTemplate.postForEntity(
                url, new HttpEntity<>(JSON.toJSONString(body), headers), String.class);

        return parseSuccessResponse(entity.getBody(), elapsed(startMs));
    }

    private JSONObject message(String role, String content) {
        JSONObject msg = new JSONObject();
        msg.put("role", role);
        msg.put("content", content);
        return msg;
    }

    private LlmResponse parseSuccessResponse(String body, long latencyMs) {
        JSONObject json = JSON.parseObject(body);
        JSONObject usage = json.getJSONObject("usage");
        int promptTokens = usage != null ? usage.getIntValue("prompt_tokens") : 0;
        int completionTokens = usage != null ? usage.getIntValue("completion_tokens") : 0;
        int totalTokens = usage != null ? usage.getIntValue("total_tokens") : promptTokens + completionTokens;

        String content = "";
        JSONArray choices = json.getJSONArray("choices");
        if (choices != null && !choices.isEmpty()) {
            JSONObject first = choices.getJSONObject(0);
            JSONObject message = first.getJSONObject("message");
            if (message != null) {
                content = message.getString("content");
            }
        }
        return LlmResponse.builder()
                .success(true)
                .degraded(false)
                .content(content)
                .errorType(AiErrorType.NONE)
                .promptTokens(promptTokens)
                .completionTokens(completionTokens)
                .totalTokens(totalTokens)
                .latencyMs(latencyMs)
                .build();
    }

    private LlmResponse tryMockFailure(long startMs) {
        String mock = aiProperties.getMockFailure();
        if (!StringUtils.hasText(mock)) {
            return null;
        }
        switch (mock.toUpperCase()) {
            case "TIMEOUT":
                return aiFallbackService.buildTimeoutResponse(elapsed(startMs));
            case "RATE_LIMIT":
                return aiFallbackService.buildRateLimitResponse(elapsed(startMs));
            case "UNAVAILABLE":
                return aiFallbackService.buildUnavailableResponse(elapsed(startMs));
            default:
                return null;
        }
    }

    private boolean isCircuitOpen() {
        return System.currentTimeMillis() < circuitOpenUntilMs;
    }

    private void markFailure() {
        if (consecutiveFailures.incrementAndGet() >= aiProperties.getCircuitBreakerThreshold()) {
            circuitOpenUntilMs = System.currentTimeMillis() + aiProperties.getCircuitBreakerOpenMs();
            consecutiveFailures.set(0);
            log.warn("AI 熔断器打开，{}ms 内走降级", aiProperties.getCircuitBreakerOpenMs());
        }
    }

    private long elapsed(long startMs) {
        return System.currentTimeMillis() - startMs;
    }

    private void sleepQuietly(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private String trimTrailingSlash(String url) {
        if (url != null && url.endsWith("/")) {
            return url.substring(0, url.length() - 1);
        }
        return url;
    }
}
