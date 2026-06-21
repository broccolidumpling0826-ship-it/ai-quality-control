package com.jhict.quality.enums;

import lombok.Getter;

@Getter
public enum EvaluationCategory {

    NORMAL("NORMAL", "正常场景"),
    BOUNDARY("BOUNDARY", "边界异常"),
    LOW_CONFIDENCE("LOW_CONFIDENCE", "低置信拒答"),
    INJECTION("INJECTION", "注入安全");

    private final String code;
    private final String label;

    EvaluationCategory(String code, String label) {
        this.code = code;
        this.label = label;
    }
}
