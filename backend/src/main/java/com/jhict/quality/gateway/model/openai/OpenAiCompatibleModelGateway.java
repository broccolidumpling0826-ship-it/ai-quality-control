package com.jhict.quality.gateway.model.openai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jhict.quality.gateway.model.ModelChatRequest;
import com.jhict.quality.gateway.model.ModelChatResponse;
import com.jhict.quality.gateway.model.ModelEmbedding;
import com.jhict.quality.gateway.model.ModelEmbeddingRequest;
import com.jhict.quality.gateway.model.ModelEmbeddingResponse;
import com.jhict.quality.gateway.model.ModelGateway;
import com.jhict.quality.gateway.model.ModelMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * OpenAI-compatible model gateway implementation for chat and embedding calls.
 */
@Slf4j
@Component
public class OpenAiCompatibleModelGateway implements ModelGateway {

    private static final String DEFAULT_CHAT_PROVIDER = "SILICONFLOW";
    private static final String OPERATION_CHAT = "chat";
    private static final String OPERATION_EMBED = "embed";
    private static final int RAW_RESPONSE_LIMIT = 4000;

    @Resource
    private OpenAiCompatibleModelProperties properties;

    @Resource
    private EmbeddingModelProperties embeddingProperties;

    @Resource
    private ObjectMapper objectMapper;

    @Override
    public ModelChatResponse chat(ModelChatRequest request) {
        long start = System.currentTimeMillis();
        String traceId = request == null ? null : request.getTraceId();
        if (!isConfigured()) {
            return buildChatFailure(request, start, notConfiguredCategory(), notConfiguredMessage());
        }
        try {
            Map<String, Object> body = buildChatBody(request);
            ResponseEntity<String> response = postJson(chatUrl(), body, effectiveTimeout(request == null ? null : request.getTimeoutMillis()));
            return parseChatResponse(request, response.getBody(), start);
        } catch (ResourceAccessException ex) {
            logGatewayFailure(OPERATION_CHAT, traceId, request == null ? null : request.getBusinessId(), "TIMEOUT_OR_IO", ex);
            return buildChatFailure(request, start, "TIMEOUT_OR_IO", "模型服务连接超时或不可达");
        } catch (RestClientResponseException ex) {
            logProviderFailure(OPERATION_CHAT, traceId, request == null ? null : request.getBusinessId(),
                    "PROVIDER_ERROR", ex.getRawStatusCode(), ex);
            return buildChatFailure(request, start, "PROVIDER_ERROR",
                    "模型服务返回错误：" + ex.getRawStatusCode(), truncate(ex.getResponseBodyAsString()));
        } catch (Exception ex) {
            logGatewayFailure(OPERATION_CHAT, traceId, request == null ? null : request.getBusinessId(), "GATEWAY_ERROR", ex);
            return buildChatFailure(request, start, "GATEWAY_ERROR", "模型网关处理失败");
        }
    }

    @Override
    public ModelEmbeddingResponse embed(ModelEmbeddingRequest request) {
        long start = System.currentTimeMillis();
        String traceId = request == null ? null : request.getTraceId();
        if (!isEmbeddingConfigured()) {
            return buildEmbeddingFailure(request, start, embeddingNotConfiguredCategory(), embeddingNotConfiguredMessage());
        }
        try {
            Map<String, Object> body = buildEmbeddingBody(request);
            ResponseEntity<String> response = postEmbeddingJson(embeddingUrl(), body,
                    effectiveEmbeddingTimeout(request == null ? null : request.getTimeoutMillis()));
            return parseEmbeddingResponse(request, response.getBody(), start);
        } catch (ResourceAccessException ex) {
            logGatewayFailure(OPERATION_EMBED, traceId, request == null ? null : request.getBusinessId(), "TIMEOUT_OR_IO", ex);
            return buildEmbeddingFailure(request, start, "TIMEOUT_OR_IO", "模型服务连接超时或不可达");
        } catch (RestClientResponseException ex) {
            logProviderFailure(OPERATION_EMBED, traceId, request == null ? null : request.getBusinessId(),
                    "PROVIDER_ERROR", ex.getRawStatusCode(), ex);
            return buildEmbeddingFailure(request, start, "PROVIDER_ERROR",
                    "模型服务返回错误：" + ex.getRawStatusCode(), truncate(ex.getResponseBodyAsString()));
        } catch (Exception ex) {
            logGatewayFailure(OPERATION_EMBED, traceId, request == null ? null : request.getBusinessId(), "GATEWAY_ERROR", ex);
            return buildEmbeddingFailure(request, start, "GATEWAY_ERROR", "模型网关处理失败");
        }
    }

    @Override
    public String provider() {
        return chatProvider();
    }

    @Override
    public boolean enabled() {
        return isConfigured() || isEmbeddingConfigured();
    }

    private Map<String, Object> buildChatBody(ModelChatRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        String modelName = choose(request == null ? null : request.getModelName(), properties.getChatModel());
        body.put("model", modelName);
        body.put("messages", buildMessages(request));
        if (request != null && request.getTemperature() != null) {
            body.put("temperature", request.getTemperature());
        }
        if (request != null && request.getMaxTokens() != null) {
            body.put("max_tokens", request.getMaxTokens());
        }
        body.put("enable_thinking", properties.isThinkingEnabled());
        if (properties.isThinkingEnabled() && supportsReasoningEffort(modelName)
                && StringUtils.hasText(properties.getReasoningEffort())) {
            body.put("reasoning_effort", properties.getReasoningEffort());
        }
        return body;
    }

    private boolean supportsReasoningEffort(String modelName) {
        return StringUtils.hasText(modelName) && modelName.contains("DeepSeek-V4-Flash");
    }

    private List<Map<String, String>> buildMessages(ModelChatRequest request) {
        List<Map<String, String>> messages = new ArrayList<>();
        if (request != null && StringUtils.hasText(request.getSystemPrompt())) {
            messages.add(message("system", request.getSystemPrompt()));
        }
        if (request != null && !CollectionUtils.isEmpty(request.getMessages())) {
            for (ModelMessage modelMessage : request.getMessages()) {
                if (modelMessage == null || !StringUtils.hasText(modelMessage.getContent())) {
                    continue;
                }
                messages.add(message(choose(modelMessage.getRole(), "user"), modelMessage.getContent()));
            }
        }
        if (messages.isEmpty()) {
            messages.add(message("user", ""));
        }
        return messages;
    }

    private Map<String, String> message(String role, String content) {
        Map<String, String> message = new LinkedHashMap<>();
        message.put("role", role);
        message.put("content", content);
        return message;
    }

    private Map<String, Object> buildEmbeddingBody(ModelEmbeddingRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", choose(request == null ? null : request.getModelName(), embeddingProperties.getModel()));
        body.put("input", request == null || CollectionUtils.isEmpty(request.getInputTexts())
                ? Collections.emptyList()
                : request.getInputTexts());
        return body;
    }

    private ResponseEntity<String> postJson(String url, Map<String, Object> body, int timeoutMillis) {
        return postJson(url, body, timeoutMillis, properties.getApiKey());
    }

    private ResponseEntity<String> postEmbeddingJson(String url, Map<String, Object> body, int timeoutMillis) {
        return postJson(url, body, timeoutMillis, embeddingProperties.getApiKey());
    }

    private ResponseEntity<String> postJson(String url, Map<String, Object> body, int timeoutMillis, String apiKey) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (StringUtils.hasText(apiKey)) {
            headers.setBearerAuth(apiKey);
        }
        return restTemplate(timeoutMillis).postForEntity(url, new HttpEntity<>(body, headers), String.class);
    }

    private RestTemplate restTemplate(int timeoutMillis) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(timeoutMillis);
        factory.setReadTimeout(timeoutMillis);
        return new RestTemplate(factory);
    }

    private ModelChatResponse parseChatResponse(ModelChatRequest request, String responseBody, long start) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode choice = root.path("choices").isArray() && root.path("choices").size() > 0
                ? root.path("choices").get(0)
                : null;
        String content = choice == null ? null : choice.path("message").path("content").asText(null);
        JsonNode usage = root.path("usage");
        return ModelChatResponse.builder()
                .success(StringUtils.hasText(content))
                .traceId(request == null ? null : request.getTraceId())
                .provider(chatProvider())
                .modelName(root.path("model").asText(choose(request == null ? null : request.getModelName(), properties.getChatModel())))
                .content(content)
                .finishReason(choice == null ? null : choice.path("finish_reason").asText(null))
                .promptTokens(intOrNull(usage, "prompt_tokens"))
                .completionTokens(intOrNull(usage, "completion_tokens"))
                .totalTokens(intOrNull(usage, "total_tokens"))
                .latencyMillis(System.currentTimeMillis() - start)
                .rawResponse(truncate(responseBody))
                .metadata(request == null ? null : request.getMetadata())
                .build();
    }

    private ModelEmbeddingResponse parseEmbeddingResponse(ModelEmbeddingRequest request, String responseBody, long start) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);
        List<ModelEmbedding> embeddings = new ArrayList<>();
        JsonNode data = root.path("data");
        if (data.isArray()) {
            for (JsonNode item : data) {
                List<Double> vector = new ArrayList<>();
                JsonNode embeddingNode = item.path("embedding");
                if (embeddingNode.isArray()) {
                    for (JsonNode number : embeddingNode) {
                        vector.add(number.asDouble());
                    }
                }
                embeddings.add(ModelEmbedding.builder()
                        .index(item.path("index").isMissingNode() ? embeddings.size() : item.path("index").asInt())
                        .inputText(inputTextAt(request, embeddings.size()))
                        .vector(vector)
                        .build());
            }
        }
        return ModelEmbeddingResponse.builder()
                .success(!embeddings.isEmpty())
                .traceId(request == null ? null : request.getTraceId())
                .provider(embeddingProvider())
                .modelName(root.path("model").asText(choose(request == null ? null : request.getModelName(), embeddingProperties.getModel())))
                .embeddings(embeddings)
                .latencyMillis(System.currentTimeMillis() - start)
                .rawResponse(truncate(responseBody))
                .metadata(request == null ? null : request.getMetadata())
                .build();
    }

    private ModelChatResponse buildChatFailure(ModelChatRequest request, long start, String errorCategory, String errorMessage) {
        return buildChatFailure(request, start, errorCategory, errorMessage, null);
    }

    private ModelChatResponse buildChatFailure(ModelChatRequest request, long start, String errorCategory,
                                               String errorMessage, String rawResponse) {
        return ModelChatResponse.builder()
                .success(false)
                .traceId(request == null ? null : request.getTraceId())
                .provider(chatProvider())
                .modelName(choose(request == null ? null : request.getModelName(), properties.getChatModel()))
                .latencyMillis(System.currentTimeMillis() - start)
                .errorCategory(errorCategory)
                .errorMessage(errorMessage)
                .rawResponse(rawResponse)
                .metadata(request == null ? null : request.getMetadata())
                .build();
    }

    private ModelEmbeddingResponse buildEmbeddingFailure(ModelEmbeddingRequest request, long start,
                                                         String errorCategory, String errorMessage) {
        return buildEmbeddingFailure(request, start, errorCategory, errorMessage, null);
    }

    private ModelEmbeddingResponse buildEmbeddingFailure(ModelEmbeddingRequest request, long start,
                                                         String errorCategory, String errorMessage,
                                                         String rawResponse) {
        return ModelEmbeddingResponse.builder()
                .success(false)
                .traceId(request == null ? null : request.getTraceId())
                .provider(embeddingProvider())
                .modelName(choose(request == null ? null : request.getModelName(), embeddingProperties.getModel()))
                .embeddings(Collections.emptyList())
                .latencyMillis(System.currentTimeMillis() - start)
                .errorCategory(errorCategory)
                .errorMessage(errorMessage)
                .rawResponse(rawResponse)
                .metadata(request == null ? null : request.getMetadata())
                .build();
    }

    private boolean isConfigured() {
        return properties.isEnabled() && StringUtils.hasText(properties.getApiKey());
    }

    private String notConfiguredCategory() {
        return properties.isEnabled() ? "MISSING_API_KEY" : "DISABLED";
    }

    private String notConfiguredMessage() {
        return properties.isEnabled() ? "模型服务未配置 API Key" : "模型服务未启用";
    }

    private boolean isEmbeddingConfigured() {
        return embeddingProperties.isEnabled() && StringUtils.hasText(embeddingProperties.getBaseUrl());
    }

    private String embeddingNotConfiguredCategory() {
        return embeddingProperties.isEnabled() ? "MISSING_BASE_URL" : "DISABLED";
    }

    private String embeddingNotConfiguredMessage() {
        return embeddingProperties.isEnabled() ? "向量模型服务未配置地址" : "向量模型服务未启用";
    }

    private int effectiveTimeout(Integer requestTimeoutMillis) {
        Integer timeout = requestTimeoutMillis != null ? requestTimeoutMillis : properties.getTimeoutMillis();
        if (timeout == null || timeout <= 0) {
            return 15000;
        }
        return timeout;
    }

    private int effectiveEmbeddingTimeout(Integer requestTimeoutMillis) {
        Integer timeout = requestTimeoutMillis != null ? requestTimeoutMillis : embeddingProperties.getTimeoutMillis();
        if (timeout == null || timeout <= 0) {
            return 15000;
        }
        return timeout;
    }

    private String chatUrl() {
        return normalizedBaseUrl() + "/chat/completions";
    }

    private String embeddingUrl() {
        return normalizedEmbeddingBaseUrl() + "/embeddings";
    }

    private String normalizedBaseUrl() {
        String baseUrl = choose(properties.getBaseUrl(), "https://api.siliconflow.cn/v1");
        while (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        return baseUrl;
    }

    private String normalizedEmbeddingBaseUrl() {
        String baseUrl = choose(embeddingProperties.getBaseUrl(), "https://api.siliconflow.cn/v1");
        while (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        return baseUrl;
    }

    private String embeddingProvider() {
        return choose(embeddingProperties.getProvider(), "OPENAI_COMPATIBLE_EMBEDDING");
    }

    private String chatProvider() {
        return choose(properties.getProvider(), DEFAULT_CHAT_PROVIDER);
    }

    private String choose(String preferred, String fallback) {
        return StringUtils.hasText(preferred) ? preferred : fallback;
    }

    private Integer intOrNull(JsonNode node, String fieldName) {
        JsonNode value = node == null ? null : node.path(fieldName);
        return value == null || value.isMissingNode() || value.isNull() ? null : value.asInt();
    }

    private String inputTextAt(ModelEmbeddingRequest request, int index) {
        if (request == null || CollectionUtils.isEmpty(request.getInputTexts()) || index >= request.getInputTexts().size()) {
            return null;
        }
        return request.getInputTexts().get(index);
    }

    private String truncate(String text) {
        if (text == null || text.length() <= RAW_RESPONSE_LIMIT) {
            return text;
        }
        return text.substring(0, RAW_RESPONSE_LIMIT);
    }

    private void logGatewayFailure(String operation, String traceId, String businessId,
                                   String errorCategory, Exception ex) {
        log.warn("模型网关调用失败，provider={}, operation={}, traceId={}, businessId={}, errorCategory={}, message={}",
                chatProvider(), operation, traceId, businessId, errorCategory, ex.getMessage());
    }

    private void logProviderFailure(String operation, String traceId, String businessId,
                                    String errorCategory, int statusCode, Exception ex) {
        log.warn("模型服务返回错误，provider={}, operation={}, traceId={}, businessId={}, errorCategory={}, statusCode={}, message={}",
                chatProvider(), operation, traceId, businessId, errorCategory, statusCode, ex.getMessage());
    }
}
