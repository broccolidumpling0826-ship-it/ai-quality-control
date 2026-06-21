package com.jhict.quality.enums;

import lombok.Getter;

@Getter
public enum AiErrorType {

    NONE("NONE", "无错误"),
    TIMEOUT("TIMEOUT", "超时"),
    RATE_LIMIT("RATE_LIMIT", "限流"),
    UNAVAILABLE("UNAVAILABLE", "服务不可用"),
    INJECTION_BLOCKED("INJECTION_BLOCKED", "注入拦截"),
    PARSE_ERROR("PARSE_ERROR", "解析错误"),
    DISABLED("DISABLED", "AI未启用");

    private final String code;
    private final String label;

    AiErrorType(String code, String label) {
        this.code = code;
        this.label = label;
    }
}
