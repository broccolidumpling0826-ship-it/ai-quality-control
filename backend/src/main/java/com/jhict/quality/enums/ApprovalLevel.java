package com.jhict.quality.enums;

import lombok.Getter;

@Getter
public enum ApprovalLevel {

    NORMAL("NORMAL", "普通审批"),
    ENHANCED("ENHANCED", "加强审批");

    private final String code;
    private final String label;

    ApprovalLevel(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public static ApprovalLevel fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (ApprovalLevel item : values()) {
            if (item.code.equals(code)) {
                return item;
            }
        }
        throw new IllegalArgumentException("未知的 ApprovalLevel code: " + code);
    }
}
