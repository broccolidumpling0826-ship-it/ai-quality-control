package com.jhict.quality.gateway.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Provider-neutral Vision OCR request for image source documents.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelVisionExtractionRequest {

    private String traceId;

    private String businessType;

    private String businessId;

    private String modelName;

    private String systemPrompt;

    private String ocrPrompt;

    /**
     * Base64-encoded image bytes without data-uri prefix.
     */
    private String imageBase64;

    /**
     * MIME type such as image/png or image/jpeg.
     */
    private String imageMimeType;

    /**
     * OpenAI-compatible image detail: auto, low, or high.
     */
    private String imageDetail;

    private Integer timeoutMillis;

    private Map<String, Object> metadata;
}
