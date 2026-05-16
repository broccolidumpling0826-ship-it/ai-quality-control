package com.jhict.quality.enums;

import lombok.Getter;

@Getter
public enum JudgmentType {

    QUALIFIED("QUALIFIED", "合格"),
    UNQUALIFIED("UNQUALIFIED", "不合格"),
    NEED_REINSPECTION("NEED_REINSPECTION", "需复检"),
    CAN_CONCESSION("CAN_CONCESSION", "可让步");

    private final String code;
    private final String label;

    JudgmentType(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public static JudgmentType fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (JudgmentType item : values()) {
            if (item.code.equals(code)) {
                return item;
            }
        }
        throw new IllegalArgumentException("未知的 JudgmentType code: " + code);
    }
}
