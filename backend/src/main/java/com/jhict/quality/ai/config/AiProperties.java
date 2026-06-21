package com.jhict.quality.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.ai")
public class AiProperties {

    /** 直接绑定 app.ai.enabled，避免嵌套歧义 */
    private boolean enabled = false;

    private ModelConfig model = new ModelConfig();
    private EmbeddingConfig embedding = new EmbeddingConfig();
    private VectorConfig vector = new VectorConfig();

    // ── 便捷方法（供 LlmClient / AiConfig 使用，无需改调用方）─────────────

    public String getApiKey() {
        return model.getApiKey();
    }

    /** chat 模型名（如 deepseek-ai/DeepSeek-V3），避免与 Lombok 生成的 getModel() 冲突 */
    public String getChatModelName() {
        return model.getChatModel();
    }

    public String getBaseUrl() {
        return model.getBaseUrl();
    }

    public int getTimeoutMs() {
        return (int) model.getTimeoutMillis();
    }

    public int getMaxRetriesOn429() {
        return model.getMaxRetriesOn429();
    }

    public int getCircuitBreakerThreshold() {
        return model.getCircuitBreakerThreshold();
    }

    public long getCircuitBreakerOpenMs() {
        return model.getCircuitBreakerOpenMs();
    }

    public String getMockFailure() {
        return model.getMockFailure();
    }

    public boolean isThinkingEnabled() {
        return model.isThinkingEnabled();
    }

    public String getReasoningEffort() {
        return model.getReasoningEffort();
    }

    // ── 子配置类 ──────────────────────────────────────────────────────────

    @Data
    public static class ModelConfig {
        private String provider = "SILICONFLOW";
        private String baseUrl = "https://api.siliconflow.cn/v1";
        private String apiKey = "";
        private String chatModel = "deepseek-ai/DeepSeek-V3";
        private boolean thinkingEnabled = false;
        private String reasoningEffort = "high";
        private long timeoutMillis = 15000;
        private int maxRetriesOn429 = 1;
        private int circuitBreakerThreshold = 3;
        private long circuitBreakerOpenMs = 60000L;
        private String mockFailure = "";
    }

    @Data
    public static class EmbeddingConfig {
        private boolean enabled = false;
        private String baseUrl = "https://api.siliconflow.cn/v1";
        private String apiKey = "";
        private String model = "BAAI/bge-m3";
        private String provider = "SILICONFLOW";
        private long timeoutMillis = 15000;
    }

    @Data
    public static class VectorConfig {
        private boolean enabled = false;
        private String host = "http://localhost:9200";
        private String username = "elastic";
        private String password = "";
        private String standardIndex = "quality-standard-clauses";
        private long timeoutMillis = 5000;
    }
}
