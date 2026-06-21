package com.jhict.quality.gateway.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Provider-neutral chat request for generated explanation and advice.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelChatRequest {

    private String traceId;

    private String businessType;

    private String businessId;

    private String promptVersion;

    private String modelName;

    private String systemPrompt;

    private List<ModelMessage> messages;

    private Double temperature;

    private Integer maxTokens;

    private Integer timeoutMillis;

    /**
     * Additional immutable facts such as citation ids, selected standard, or confidence factors.
     */
    private Map<String, Object> metadata;
}
