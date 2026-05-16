package com.jhict.quality.enums;

import lombok.Getter;

@Getter
public enum InspectionStatus {

    NORMAL("NORMAL", "正常"),
    VOID("VOID", "作废");

    private final String code;
    private final String label;

    InspectionStatus(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public static InspectionStatus fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (InspectionStatus item : values()) {
            if (item.code.equals(code)) {
                return item;
            }
        }
        throw new IllegalArgumentException("未知的 InspectionStatus code: " + code);
    }
}
