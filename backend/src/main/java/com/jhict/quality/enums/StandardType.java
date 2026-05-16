package com.jhict.quality.enums;

import lombok.Getter;

@Getter
public enum StandardType {

    NATIONAL("NATIONAL", "国标"),
    ENTERPRISE("ENTERPRISE", "企标"),
    CUSTOMER("CUSTOMER", "客户协议");

    private final String code;
    private final String label;

    StandardType(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public static StandardType fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (StandardType item : values()) {
            if (item.code.equals(code)) {
                return item;
            }
        }
        throw new IllegalArgumentException("未知的 StandardType code: " + code);
    }
}
