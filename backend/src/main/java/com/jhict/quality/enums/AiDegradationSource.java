package com.jhict.quality.enums;

import lombok.Getter;

@Getter
public enum AiDegradationSource {

    GENERATED("GENERATED", "模型生成"),
    CACHE("CACHE", "缓存输出"),
    RULE_TEMPLATE("RULE_TEMPLATE", "规则模板"),
    RAW_RETRIEVAL("RAW_RETRIEVAL", "原始检索"),
    UNAVAILABLE("UNAVAILABLE", "服务不可用");

    private final String code;
    private final String label;

    AiDegradationSource(String code, String label) {
        this.code = code;
        this.label = label;
    }
}
