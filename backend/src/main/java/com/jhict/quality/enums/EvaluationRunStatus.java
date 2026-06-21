package com.jhict.quality.enums;

import lombok.Getter;

@Getter
public enum EvaluationRunStatus {

    RUNNING("RUNNING", "运行中"),
    COMPLETED("COMPLETED", "已完成"),
    FAILED("FAILED", "失败");

    private final String code;
    private final String label;

    EvaluationRunStatus(String code, String label) {
        this.code = code;
        this.label = label;
    }
}
