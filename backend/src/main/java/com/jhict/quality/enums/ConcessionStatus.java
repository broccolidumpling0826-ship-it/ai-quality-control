package com.jhict.quality.enums;

import lombok.Getter;

@Getter
public enum ConcessionStatus {

    PENDING_APPROVAL("PENDING_APPROVAL", "待审批"),
    SALES_APPROVED("SALES_APPROVED", "销售已审批"),
    APPROVED("APPROVED", "已批准"),
    INVALID("INVALID", "已作废"),
    REJECTED("REJECTED", "已拒绝");

    private final String code;
    private final String label;

    ConcessionStatus(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public static ConcessionStatus fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (ConcessionStatus item : values()) {
            if (item.code.equals(code)) {
                return item;
            }
        }
        throw new IllegalArgumentException("未知的 ConcessionStatus code: " + code);
    }
}
