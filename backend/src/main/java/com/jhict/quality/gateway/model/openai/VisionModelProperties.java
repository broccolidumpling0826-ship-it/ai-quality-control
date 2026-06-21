package com.jhict.quality.gateway.model.openai;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Environment-backed configuration for Vision OCR calls on the chat model gateway.
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.ai.model.vision")
public class VisionModelProperties {

    private boolean enabled = false;

    private String model = "deepseek-ai/DeepSeek-OCR";

    private String ocrPrompt = "<image>\n<|grounding|>Convert the document to markdown.";

    private String imageDetail = "high";

    private Integer timeoutMillis = 60000;
}
