package com.jhict.quality.gateway.vector;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Provider-neutral standard clause retrieval request.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VectorSearchRequest {

    private String traceId;

    private String businessType;

    private String businessId;

    private String indexName;

    private String queryText;

    private List<Double> queryVector;

    private Integer topK;

    private Double answerMinScore;

    private Double displayMinScore;

    private List<String> sourceTypes;

    private String customerId;

    private String variety;

    private String grade;

    private String spec;

    private String usageScope;

    private String indicatorCode;

    private String effectiveDate;

    private Map<String, Object> metadata;
}
