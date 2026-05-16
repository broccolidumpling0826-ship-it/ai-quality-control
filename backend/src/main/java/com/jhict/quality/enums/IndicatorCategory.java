package com.jhict.quality.enums;

import lombok.Getter;

@Getter
public enum IndicatorCategory {

    COMPOSITION("COMPOSITION", "成分"),
    PERFORMANCE("PERFORMANCE", "性能"),
    DIMENSION("DIMENSION", "尺寸"),
    SURFACE("SURFACE", "表面"),
    SHAPE("SHAPE", "形状");

    private final String code;
    private final String label;

    IndicatorCategory(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public static IndicatorCategory fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (IndicatorCategory item : values()) {
            if (item.code.equals(code)) {
                return item;
            }
        }
        throw new IllegalArgumentException("未知的 IndicatorCategory code: " + code);
    }
}
