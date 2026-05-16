package com.jhict.quality.enums;

import lombok.Getter;

@Getter
public enum ApprovalStatus {

    PENDING("PENDING", "待审批"),
    APPROVED("APPROVED", "已批准"),
    REJECTED("REJECTED", "已拒绝");

    private final String code;
    private final String label;

    ApprovalStatus(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public static ApprovalStatus fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (ApprovalStatus item : values()) {
            if (item.code.equals(code)) {
                return item;
            }
        }
        throw new IllegalArgumentException("未知的 ApprovalStatus code: " + code);
    }
}
