package com.jhict.quality.vo;

import lombok.Data;

/**
 * Source reference shown beside AI/degraded answers.
 */
@Data
public class AiSourceReferenceVO {

    private String clauseId;

    private String documentId;

    private String sourceType;

    private String standardCode;

    private String standardName;

    private String versionNo;

    private String clauseNo;

    private Integer pageNo;

    private String paragraphText;

    private Double score;
}
