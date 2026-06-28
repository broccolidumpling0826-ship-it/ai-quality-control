package com.jhict.quality.service.support.prompt;

import com.jhict.quality.gateway.model.openai.VisionModelProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class VisionOcrPromptProvider {

    private final PromptRegistry promptRegistry;
    private final VisionModelProperties visionProperties;

    public VisionOcrPromptProvider(PromptRegistry promptRegistry, VisionModelProperties visionProperties) {
        this.promptRegistry = promptRegistry;
        this.visionProperties = visionProperties;
    }

    public String resolveSystemPrompt() {
        return promptRegistry.resolve(PromptScene.VISION_OCR).getSystemPrompt();
    }

    public String resolveOcrPrompt() {
        if (visionProperties != null && StringUtils.hasText(visionProperties.getOcrPrompt())) {
            return visionProperties.getOcrPrompt();
        }
        PromptTemplate template = promptRegistry.resolve(PromptScene.VISION_OCR);
        return template.getUserPrompt();
    }

    public String activePromptVersion() {
        return promptRegistry.activeVersion(PromptScene.VISION_OCR);
    }
}
