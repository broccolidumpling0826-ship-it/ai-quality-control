package com.jhict.quality.gateway.vector.elasticsearch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jhict.quality.gateway.vector.VectorClauseDocument;
import com.jhict.quality.gateway.vector.VectorDeleteRequest;
import com.jhict.quality.gateway.vector.VectorDeleteResponse;
import com.jhict.quality.gateway.vector.VectorIndexRequest;
import com.jhict.quality.gateway.vector.VectorIndexResponse;
import com.jhict.quality.gateway.vector.VectorSearchRequest;
import com.jhict.quality.gateway.vector.VectorSearchResponse;
import com.jhict.quality.gateway.vector.VectorSearchResult;
import com.jhict.quality.gateway.vector.VectorStoreGateway;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Elasticsearch 8.x HTTP implementation for standard clause indexing and search.
 */
@Slf4j
@Component
public class ElasticsearchVectorStoreGateway implements VectorStoreGateway {

    private static final String PROVIDER = "ELASTICSEARCH_8";
    private static final String OPERATION_INDEX = "indexClauses";
    private static final String OPERATION_SEARCH = "searchClauses";
    private static final String OPERATION_DELETE = "deleteClause";

    @Resource
    private ElasticsearchVectorProperties properties;

    @Resource
    private ObjectMapper objectMapper;

    @Override
    public VectorIndexResponse indexClauses(VectorIndexRequest request) {
        long start = System.currentTimeMillis();
        if (!enabled()) {
            return indexFailure(request, start, notConfiguredCategory(), notConfiguredMessage());
        }
        if (request == null || CollectionUtils.isEmpty(request.getClauses())) {
            return VectorIndexResponse.builder()
                    .success(true)
                    .traceId(request == null ? null : request.getTraceId())
                    .provider(PROVIDER)
                    .indexName(indexName(request == null ? null : request.getIndexName()))
                    .indexedCount(0)
                    .failedClauseIds(Collections.emptyList())
                    .latencyMillis(System.currentTimeMillis() - start)
                    .metadata(request == null ? null : request.getMetadata())
                    .build();
        }
        List<String> failedClauseIds = new ArrayList<>();
        int indexedCount = 0;
        for (VectorClauseDocument clause : request.getClauses()) {
            if (clause == null || !StringUtils.hasText(clause.getClauseId())) {
                continue;
            }
            try {
                String url = documentUrl(indexName(request.getIndexName()), clause.getClauseId());
                ResponseEntity<String> response = exchange(url, HttpMethod.PUT, toDocumentBody(clause),
                        effectiveTimeout(null));
                if (response.getStatusCode().is2xxSuccessful()) {
                    indexedCount++;
                } else {
                    failedClauseIds.add(clause.getClauseId());
                }
            } catch (Exception ex) {
                failedClauseIds.add(clause.getClauseId());
                logGatewayFailure(OPERATION_INDEX, request.getTraceId(), request.getBusinessId(),
                        "INDEX_ITEM_ERROR", ex);
            }
        }
        return VectorIndexResponse.builder()
                .success(failedClauseIds.isEmpty())
                .traceId(request.getTraceId())
                .provider(PROVIDER)
                .indexName(indexName(request.getIndexName()))
                .indexedCount(indexedCount)
                .failedClauseIds(failedClauseIds)
                .latencyMillis(System.currentTimeMillis() - start)
                .metadata(request.getMetadata())
                .build();
    }

    @Override
    public VectorSearchResponse searchClauses(VectorSearchRequest request) {
        long start = System.currentTimeMillis();
        if (!enabled()) {
            return searchFailure(request, start, notConfiguredCategory(), notConfiguredMessage());
        }
        try {
            String indexName = indexName(request == null ? null : request.getIndexName());
            Map<String, Object> body = buildSearchBody(request);
            ResponseEntity<String> response = exchange(searchUrl(indexName), HttpMethod.POST, body,
                    effectiveTimeout(null));
            return parseSearchResponse(request, indexName, response.getBody(), start);
        } catch (ResourceAccessException ex) {
            logGatewayFailure(OPERATION_SEARCH, request == null ? null : request.getTraceId(),
                    request == null ? null : request.getBusinessId(), "TIMEOUT_OR_IO", ex);
            return searchFailure(request, start, "TIMEOUT_OR_IO", "向量检索服务连接超时或不可达");
        } catch (RestClientResponseException ex) {
            logProviderFailure(OPERATION_SEARCH, request == null ? null : request.getTraceId(),
                    request == null ? null : request.getBusinessId(), "PROVIDER_ERROR", ex.getRawStatusCode(), ex);
            return searchFailure(request, start, "PROVIDER_ERROR", "向量检索服务返回错误：" + ex.getRawStatusCode());
        } catch (Exception ex) {
            logGatewayFailure(OPERATION_SEARCH, request == null ? null : request.getTraceId(),
                    request == null ? null : request.getBusinessId(), "GATEWAY_ERROR", ex);
            return searchFailure(request, start, "GATEWAY_ERROR", "向量检索网关处理失败");
        }
    }

    @Override
    public VectorDeleteResponse deleteClause(VectorDeleteRequest request) {
        long start = System.currentTimeMillis();
        if (!enabled()) {
            return deleteFailure(request, start, notConfiguredCategory(), notConfiguredMessage());
        }
        try {
            String documentId = choose(request == null ? null : request.getEsDocumentKey(),
                    request == null ? null : request.getClauseId());
            if (!StringUtils.hasText(documentId)) {
                return deleteFailure(request, start, "INVALID_INPUT", "缺少待删除条款ID");
            }
            String indexName = indexName(request == null ? null : request.getIndexName());
            ResponseEntity<String> response = exchange(documentUrl(indexName, documentId), HttpMethod.DELETE,
                    Collections.emptyMap(), effectiveTimeout(null));
            return VectorDeleteResponse.builder()
                    .success(response.getStatusCode().is2xxSuccessful())
                    .traceId(request == null ? null : request.getTraceId())
                    .provider(PROVIDER)
                    .indexName(indexName)
                    .clauseId(request == null ? null : request.getClauseId())
                    .latencyMillis(System.currentTimeMillis() - start)
                    .metadata(request == null ? null : request.getMetadata())
                    .build();
        } catch (ResourceAccessException ex) {
            logGatewayFailure(OPERATION_DELETE, request == null ? null : request.getTraceId(),
                    request == null ? null : request.getClauseId(), "TIMEOUT_OR_IO", ex);
            return deleteFailure(request, start, "TIMEOUT_OR_IO", "向量检索服务连接超时或不可达");
        } catch (RestClientResponseException ex) {
            logProviderFailure(OPERATION_DELETE, request == null ? null : request.getTraceId(),
                    request == null ? null : request.getClauseId(), "PROVIDER_ERROR", ex.getRawStatusCode(), ex);
            return deleteFailure(request, start, "PROVIDER_ERROR", "向量检索服务返回错误：" + ex.getRawStatusCode());
        } catch (Exception ex) {
            logGatewayFailure(OPERATION_DELETE, request == null ? null : request.getTraceId(),
                    request == null ? null : request.getClauseId(), "GATEWAY_ERROR", ex);
            return deleteFailure(request, start, "GATEWAY_ERROR", "向量检索网关处理失败");
        }
    }

    @Override
    public String provider() {
        return PROVIDER;
    }

    @Override
    public boolean enabled() {
        return properties.isEnabled() && StringUtils.hasText(properties.getHost());
    }

    private Map<String, Object> toDocumentBody(VectorClauseDocument clause) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("clauseId", clause.getClauseId());
        body.put("documentId", clause.getDocumentId());
        body.put("clauseKey", clause.getClauseKey());
        body.put("clauseNo", clause.getClauseNo());
        body.put("pageNo", clause.getPageNo());
        body.put("paragraphText", clause.getParagraphText());
        body.put("sourceType", clause.getSourceType());
        body.put("standardType", clause.getStandardType());
        body.put("standardCode", clause.getStandardCode());
        body.put("standardName", clause.getStandardName());
        body.put("versionNo", clause.getVersionNo());
        body.put("customerId", clause.getCustomerId());
        body.put("variety", clause.getVariety());
        body.put("grade", clause.getGrade());
        body.put("specRange", clause.getSpecRange());
        body.put("usageScope", clause.getUsageScope());
        body.put("indicatorId", clause.getIndicatorId());
        body.put("indicatorCode", clause.getIndicatorCode());
        body.put("indicatorName", clause.getIndicatorName());
        body.put("effectiveDate", clause.getEffectiveDate());
        body.put("expiryDate", clause.getExpiryDate());
        body.put("retrievalKeywords", clause.getRetrievalKeywords());
        body.put("metadata", clause.getMetadata());
        return body;
    }

    private Map<String, Object> buildSearchBody(VectorSearchRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("size", request == null || request.getTopK() == null ? 10 : request.getTopK());
        body.put("query", buildSearchQuery(request));
        body.put("highlight", buildHighlight());
        return body;
    }

    private Map<String, Object> buildSearchQuery(VectorSearchRequest request) {
        Map<String, Object> bool = new LinkedHashMap<>();
        List<Object> must = new ArrayList<>();
        if (request != null && StringUtils.hasText(request.getQueryText())) {
            Map<String, Object> multiMatch = new LinkedHashMap<>();
            multiMatch.put("query", request.getQueryText());
            multiMatch.put("fields", new String[]{"paragraphText^4", "retrievalKeywords^3", "standardName^2", "indicatorName^2", "standardCode"});
            Map<String, Object> query = new LinkedHashMap<>();
            query.put("multi_match", multiMatch);
            must.add(query);
        } else {
            must.add(Collections.singletonMap("match_all", Collections.emptyMap()));
        }
        bool.put("must", must);
        bool.put("filter", buildFilters(request));

        Map<String, Object> boolWrapper = new LinkedHashMap<>();
        boolWrapper.put("bool", bool);

        if (request != null && !CollectionUtils.isEmpty(request.getQueryVector())) {
            Map<String, Object> script = new LinkedHashMap<>();
            script.put("source", "cosineSimilarity(params.query_vector, 'embedding') + 1.0");
            script.put("params", Collections.singletonMap("query_vector", request.getQueryVector()));
            Map<String, Object> scriptScore = new LinkedHashMap<>();
            scriptScore.put("query", boolWrapper);
            scriptScore.put("script", script);
            return Collections.singletonMap("script_score", scriptScore);
        }
        return boolWrapper;
    }

    private List<Object> buildFilters(VectorSearchRequest request) {
        List<Object> filters = new ArrayList<>();
        if (request == null) {
            return filters;
        }
        if (!CollectionUtils.isEmpty(request.getSourceTypes())) {
            filters.add(Collections.singletonMap("terms", Collections.singletonMap("sourceType.keyword", request.getSourceTypes())));
        }
        addTermFilter(filters, "customerId.keyword", request.getCustomerId());
        addTermFilter(filters, "variety.keyword", request.getVariety());
        addTermFilter(filters, "grade.keyword", request.getGrade());
        addTermFilter(filters, "indicatorCode.keyword", request.getIndicatorCode());
        if (StringUtils.hasText(request.getEffectiveDate())) {
            filters.add(Collections.singletonMap("range",
                    Collections.singletonMap("effectiveDate", Collections.singletonMap("lte", request.getEffectiveDate()))));
            filters.add(Collections.singletonMap("range",
                    Collections.singletonMap("expiryDate", Collections.singletonMap("gte", request.getEffectiveDate()))));
        }
        return filters;
    }

    private void addTermFilter(List<Object> filters, String field, String value) {
        if (StringUtils.hasText(value)) {
            filters.add(Collections.singletonMap("term", Collections.singletonMap(field, value)));
        }
    }

    private Map<String, Object> buildHighlight() {
        Map<String, Object> paragraph = new LinkedHashMap<>();
        paragraph.put("number_of_fragments", 1);
        paragraph.put("fragment_size", 180);
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("paragraphText", paragraph);
        Map<String, Object> highlight = new LinkedHashMap<>();
        highlight.put("fields", fields);
        return highlight;
    }

    private VectorSearchResponse parseSearchResponse(VectorSearchRequest request, String indexName,
                                                     String responseBody, long start) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode hits = root.path("hits").path("hits");
        List<VectorSearchResult> results = new ArrayList<>();
        if (hits.isArray()) {
            for (JsonNode hit : hits) {
                JsonNode source = hit.path("_source");
                results.add(VectorSearchResult.builder()
                        .clauseId(source.path("clauseId").asText(hit.path("_id").asText(null)))
                        .documentId(source.path("documentId").asText(null))
                        .score(hit.path("_score").isNumber() ? hit.path("_score").asDouble() : null)
                        .sourceType(source.path("sourceType").asText(null))
                        .standardType(source.path("standardType").asText(null))
                        .standardCode(source.path("standardCode").asText(null))
                        .standardName(source.path("standardName").asText(null))
                        .versionNo(source.path("versionNo").asText(null))
                        .clauseNo(source.path("clauseNo").asText(null))
                        .pageNo(source.path("pageNo").isNumber() ? source.path("pageNo").asInt() : null)
                        .paragraphText(source.path("paragraphText").asText(null))
                        .highlightText(firstHighlight(hit))
                        .customerId(source.path("customerId").asText(null))
                        .variety(source.path("variety").asText(null))
                        .grade(source.path("grade").asText(null))
                        .indicatorCode(source.path("indicatorCode").asText(null))
                        .indicatorName(source.path("indicatorName").asText(null))
                        .build());
            }
        }
        return VectorSearchResponse.builder()
                .success(true)
                .traceId(request == null ? null : request.getTraceId())
                .provider(PROVIDER)
                .indexName(indexName)
                .results(results)
                .latencyMillis(System.currentTimeMillis() - start)
                .metadata(request == null ? null : request.getMetadata())
                .build();
    }

    private String firstHighlight(JsonNode hit) {
        JsonNode values = hit.path("highlight").path("paragraphText");
        if (values.isArray() && values.size() > 0) {
            return values.get(0).asText(null);
        }
        return null;
    }

    private ResponseEntity<String> exchange(String url, HttpMethod method, Map<String, Object> body, int timeoutMillis) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (StringUtils.hasText(properties.getUsername()) && StringUtils.hasText(properties.getPassword())) {
            headers.setBasicAuth(properties.getUsername(), properties.getPassword());
        }
        return restTemplate(timeoutMillis).exchange(url, method, new HttpEntity<>(body, headers), String.class);
    }

    private RestTemplate restTemplate(int timeoutMillis) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(timeoutMillis);
        factory.setReadTimeout(timeoutMillis);
        return new RestTemplate(factory);
    }

    private VectorIndexResponse indexFailure(VectorIndexRequest request, long start, String errorCategory, String errorMessage) {
        return VectorIndexResponse.builder()
                .success(false)
                .traceId(request == null ? null : request.getTraceId())
                .provider(PROVIDER)
                .indexName(indexName(request == null ? null : request.getIndexName()))
                .indexedCount(0)
                .failedClauseIds(Collections.emptyList())
                .latencyMillis(System.currentTimeMillis() - start)
                .errorCategory(errorCategory)
                .errorMessage(errorMessage)
                .metadata(request == null ? null : request.getMetadata())
                .build();
    }

    private VectorSearchResponse searchFailure(VectorSearchRequest request, long start, String errorCategory, String errorMessage) {
        return VectorSearchResponse.builder()
                .success(false)
                .traceId(request == null ? null : request.getTraceId())
                .provider(PROVIDER)
                .indexName(indexName(request == null ? null : request.getIndexName()))
                .results(Collections.emptyList())
                .latencyMillis(System.currentTimeMillis() - start)
                .errorCategory(errorCategory)
                .errorMessage(errorMessage)
                .metadata(request == null ? null : request.getMetadata())
                .build();
    }

    private VectorDeleteResponse deleteFailure(VectorDeleteRequest request, long start, String errorCategory, String errorMessage) {
        return VectorDeleteResponse.builder()
                .success(false)
                .traceId(request == null ? null : request.getTraceId())
                .provider(PROVIDER)
                .indexName(indexName(request == null ? null : request.getIndexName()))
                .clauseId(request == null ? null : request.getClauseId())
                .latencyMillis(System.currentTimeMillis() - start)
                .errorCategory(errorCategory)
                .errorMessage(errorMessage)
                .metadata(request == null ? null : request.getMetadata())
                .build();
    }

    private String searchUrl(String indexName) {
        return normalizedHost() + "/" + encode(indexName) + "/_search";
    }

    private String documentUrl(String indexName, String documentId) {
        return normalizedHost() + "/" + encode(indexName) + "/_doc/" + encode(documentId);
    }

    private String indexName(String requestIndexName) {
        return choose(requestIndexName, properties.getStandardIndex());
    }

    private String normalizedHost() {
        String host = choose(properties.getHost(), "http://localhost:9200");
        while (host.endsWith("/")) {
            host = host.substring(0, host.length() - 1);
        }
        return host;
    }

    private int effectiveTimeout(Integer requestTimeoutMillis) {
        Integer timeout = requestTimeoutMillis != null ? requestTimeoutMillis : properties.getTimeoutMillis();
        if (timeout == null || timeout <= 0) {
            return 5000;
        }
        return timeout;
    }

    private String choose(String preferred, String fallback) {
        return StringUtils.hasText(preferred) ? preferred : fallback;
    }

    private String encode(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            throw new IllegalStateException("UTF-8 encoding is unavailable", ex);
        }
    }

    private String notConfiguredCategory() {
        return properties.isEnabled() ? "MISSING_HOST" : "DISABLED";
    }

    private String notConfiguredMessage() {
        return properties.isEnabled() ? "向量检索服务未配置地址" : "向量检索服务未启用";
    }

    private void logGatewayFailure(String operation, String traceId, String businessId,
                                   String errorCategory, Exception ex) {
        log.warn("向量网关调用失败，provider={}, operation={}, traceId={}, businessId={}, errorCategory={}, message={}",
                PROVIDER, operation, traceId, businessId, errorCategory, ex.getMessage());
    }

    private void logProviderFailure(String operation, String traceId, String businessId,
                                    String errorCategory, int statusCode, Exception ex) {
        log.warn("向量服务返回错误，provider={}, operation={}, traceId={}, businessId={}, errorCategory={}, statusCode={}, message={}",
                PROVIDER, operation, traceId, businessId, errorCategory, statusCode, ex.getMessage());
    }
}
