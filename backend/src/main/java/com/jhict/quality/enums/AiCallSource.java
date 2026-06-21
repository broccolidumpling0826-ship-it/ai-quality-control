package com.jhict.quality.enums;

import lombok.Getter;

@Getter
public enum AiCallSource {

    RAG_QUERY("RAG_QUERY", "标准RAG检索"),
    JUDGMENT_EXPLAIN("JUDGMENT_EXPLAIN", "AI判定解释"),
    CONCESSION_ASSESS("CONCESSION_ASSESS", "让步风险评估"),
    REINSPECTION_ADVICE("REINSPECTION_ADVICE", "复检建议"),
    REJUDGMENT_ADVICE("REJUDGMENT_ADVICE", "改判建议"),
    CERT_SUMMARY("CERT_SUMMARY", "质保书说明"),
    EVALUATION("EVALUATION", "评测运行");

    private final String code;
    private final String label;

    AiCallSource(String code, String label) {
        this.code = code;
        this.label = label;
    }
}
