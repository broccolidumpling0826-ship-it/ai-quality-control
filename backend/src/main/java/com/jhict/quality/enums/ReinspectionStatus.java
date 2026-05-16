package com.jhict.quality.enums;

import lombok.Getter;

@Getter
public enum ReinspectionStatus {

    PENDING("PENDING", "待复检"),
    COMPLETED("COMPLETED", "已完成");

    private final String code;
    private final String label;

    ReinspectionStatus(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public static ReinspectionStatus fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (ReinspectionStatus item : values()) {
            if (item.code.equals(code)) {
                return item;
            }
        }
        throw new IllegalArgumentException("未知的 ReinspectionStatus code: " + code);
    }
}
