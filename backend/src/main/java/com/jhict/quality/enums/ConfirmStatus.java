package com.jhict.quality.enums;

import lombok.Getter;

@Getter
public enum ConfirmStatus {

    PENDING("PENDING", "待确认"),
    CONFIRMED("CONFIRMED", "已确认"),
    REJECTED("REJECTED", "已拒绝");

    private final String code;
    private final String label;

    ConfirmStatus(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public static ConfirmStatus fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (ConfirmStatus item : values()) {
            if (item.code.equals(code)) {
                return item;
            }
        }
        throw new IllegalArgumentException("未知的 ConfirmStatus code: " + code);
    }
}
