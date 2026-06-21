package com.jhict.quality.gateway.vector;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.List;

/**
 * Clause document mirrored from qc_standard_clause to the vector store.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VectorClauseDocument {

    private String clauseId;

    private String documentId;

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

    private String effectiveDate;

    private String expiryDate;

    private String retrievalKeywords;

    private String sourceFileName;

    private List<Double> embedding;

    private Map<String, Object> metadata;
}
