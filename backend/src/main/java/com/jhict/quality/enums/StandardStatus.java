package com.jhict.quality.enums;

import lombok.Getter;

@Getter
public enum StandardStatus {

    DRAFT("DRAFT", "草稿"),
    PUBLISHED("PUBLISHED", "已发布"),
    DEPRECATED("DEPRECATED", "已废弃");

    private final String code;
    private final String label;

    StandardStatus(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public static StandardStatus fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (StandardStatus item : values()) {
            if (item.code.equals(code)) {
                return item;
            }
        }
        throw new IllegalArgumentException("未知的 StandardStatus code: " + code);
    }
}
