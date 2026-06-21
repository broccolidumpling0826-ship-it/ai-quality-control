package com.jhict.quality.gateway.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Provider-neutral chat message.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelMessage {

    /**
     * Role such as system, user, assistant, or tool.
     */
    private String role;

    /**
     * Message content. Treat as untrusted text.
     */
    private String content;
}
