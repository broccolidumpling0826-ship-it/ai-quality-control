package com.jhict.quality.gateway.model;

import lombok.Getter;

/**
 * Runtime exception used by model gateway implementations after sanitizing provider details.
 */
@Getter
public class ModelGatewayException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String provider;
    private final String operation;
    private final String errorCategory;
    private final String traceId;

    public ModelGatewayException(String provider, String operation, String errorCategory,
                                 String traceId, String message) {
        super(message);
        this.provider = provider;
        this.operation = operation;
        this.errorCategory = errorCategory;
        this.traceId = traceId;
    }

    public ModelGatewayException(String provider, String operation, String errorCategory,
                                 String traceId, String message, Throwable cause) {
        super(message, cause);
        this.provider = provider;
        this.operation = operation;
        this.errorCategory = errorCategory;
        this.traceId = traceId;
    }
}
