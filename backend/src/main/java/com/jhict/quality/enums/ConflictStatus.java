package com.jhict.quality.enums;

import lombok.Getter;

@Getter
public enum ConflictStatus {

    PENDING("PENDING", "待裁定"),
    RESOLVED_CUSTOMER("RESOLVED_CUSTOMER", "以客协为准"),
    RESOLVED_ENTERPRISE("RESOLVED_ENTERPRISE", "以企标为准"),
    RESOLVED_MANUAL("RESOLVED_MANUAL", "人工裁定");

    private final String code;
    private final String label;

    ConflictStatus(String code, String label) {
        this.code = code;
        this.label = label;
    }
}
