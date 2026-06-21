package com.jhict.quality.vo;

import lombok.Data;

import java.time.LocalDate;

@Data
public class StandardClauseVO {

    private String id;

    private String documentId;

    private String standardId;

    private String clauseKey;

    private String clauseNo;

    private Integer pageNo;

    private String paragraphText;

    private String sourceType;

    private String standardType;

    private String standardCode;

    private String standardName;

    private String versionNo;

    private String customerId;

    private String variety;

    private String grade;

    private String specRange;

    private String usageScope;

    private String indicatorId;

    private String indicatorCode;

    private String indicatorName;

    private LocalDate effectiveDate;

    private LocalDate expiryDate;

    private String retrievalKeywords;

    private String esDocumentKey;

    private String embeddingStatus;

    private String relevanceGroup;

    private String status;
}
