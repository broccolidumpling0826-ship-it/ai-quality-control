package com.jhict.quality.enums;

import lombok.Getter;

@Getter
public enum AiDegradationSource {

    GENERATED("GENERATED", "AI生成"),
    CACHE("CACHE", "缓存回答"),
    RULE_TEMPLATE("RULE_TEMPLATE", "规则模板"),
    RAW_RETRIEVAL("RAW_RETRIEVAL", "仅展示条款"),
    UNAVAILABLE("UNAVAILABLE", "无引用依据");

    private final String code;
    private final String label;

    AiDegradationSource(String code, String label) {
        this.code = code;
        this.label = label;
    }
}
