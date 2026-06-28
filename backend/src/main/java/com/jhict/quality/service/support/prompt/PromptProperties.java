package com.jhict.quality.service.support.prompt;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.ai.prompts")
public class PromptProperties {

    /**
     * Classpath or file location for prompt manifest and templates.
     */
    private String basePath = "classpath:ai-prompts/";
}
