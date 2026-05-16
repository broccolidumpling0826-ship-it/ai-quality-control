package com.jhict.quality.enums;

import lombok.Getter;

@Getter
public enum SampleType {

    HEAD("HEAD", "头样"),
    TAIL("TAIL", "尾样"),
    MIDDLE("MIDDLE", "中间样");

    private final String code;
    private final String label;

    SampleType(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public static SampleType fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (SampleType item : values()) {
            if (item.code.equals(code)) {
                return item;
            }
        }
        throw new IllegalArgumentException("未知的 SampleType code: " + code);
    }
}
