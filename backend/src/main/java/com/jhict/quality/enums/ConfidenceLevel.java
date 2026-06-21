package com.jhict.quality.enums;

import lombok.Getter;

@Getter
public enum ConfidenceLevel {

    HIGH("HIGH", "高"),
    MEDIUM("MEDIUM", "中"),
    LOW("LOW", "低");

    private final String code;
    private final String label;

    ConfidenceLevel(String code, String label) {
        this.code = code;
        this.label = label;
    }
}
