package com.jhict.quality.service.support.prompt;

/**
 * Registered AI prompt scenes backed by classpath templates.
 */
public enum PromptScene {

    JUDGMENT_EXPLANATION("judgment-explanation"),
    STANDARD_RAG("standard-rag"),
    CERT_QA("cert-qa"),
    CONCESSION_RISK("concession-risk"),
    VISION_OCR("vision-ocr");

    private final String resourceFolder;

    PromptScene(String resourceFolder) {
        this.resourceFolder = resourceFolder;
    }

    public String getResourceFolder() {
        return resourceFolder;
    }

    public static PromptScene fromManifestKey(String key) {
        return PromptScene.valueOf(key);
    }
}
