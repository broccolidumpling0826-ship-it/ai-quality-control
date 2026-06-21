package com.jhict.quality.gateway.vector;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * One retrieved standard/agreement/case clause.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VectorSearchResult {

    private String clauseId;

    private String documentId;

    private Double score;

    private String sourceType;

    private String standardType;

    private String standardCode;

    private String standardName;

    private String versionNo;

    private String clauseNo;

    private Integer pageNo;

    private String paragraphText;

    private String highlightText;

    private String customerId;

    private String variety;

    private String grade;

    private String indicatorCode;

    private String indicatorName;

    private String sourceFileName;

    private Map<String, Object> metadata;
}
