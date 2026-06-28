package com.jhict.quality.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jhict.quality.common.entity.ApiResult;
import com.jhict.quality.common.exception.ServiceException;
import com.jhict.quality.dto.AiDegradationRequest;
import com.jhict.quality.dto.StandardClausePageQuery;
import com.jhict.quality.dto.StandardRagQueryCmd;
import com.jhict.quality.entity.QcQualityStandard;
import com.jhict.quality.entity.QcStandardClause;
import com.jhict.quality.enums.AiDegradationSource;
import com.jhict.quality.gateway.model.ModelEmbedding;
import com.jhict.quality.gateway.model.ModelEmbeddingRequest;
import com.jhict.quality.gateway.model.ModelEmbeddingResponse;
import com.jhict.quality.gateway.model.ModelChatRequest;
import com.jhict.quality.gateway.model.ModelChatResponse;
import com.jhict.quality.gateway.model.ModelGateway;
import com.jhict.quality.gateway.model.ModelMessage;
import com.jhict.quality.gateway.vector.VectorSearchRequest;
import com.jhict.quality.gateway.vector.VectorSearchResponse;
import com.jhict.quality.gateway.vector.VectorSearchResult;
import com.jhict.quality.gateway.vector.VectorStoreGateway;
import com.jhict.quality.mapper.QcQualityStandardMapper;
import com.jhict.quality.mapper.QcStandardClauseMapper;
import com.jhict.quality.service.api.AiDegradationService;
import com.jhict.quality.service.api.StandardDocumentService;
import com.jhict.quality.service.api.StandardRagService;
import com.jhict.quality.service.support.prompt.StandardRagPromptBuilder;
import com.jhict.quality.service.support.rag.CitationReferenceSupport;
import com.jhict.quality.vo.AiDegradationResultVO;
import com.jhict.quality.vo.AiSourceReferenceVO;
import com.jhict.quality.vo.StandardClauseVO;
import com.jhict.quality.vo.StandardRagAnswerVO;
import com.jhict.quality.vo.StandardRagSourceVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class StandardRagServiceImpl implements StandardRagService {

    private static final int DEFAULT_TOP_K = 5;
    private static final double HIGH_CONFIDENCE_SCORE = 0.86D;
    private static final double MEDIUM_CONFIDENCE_SCORE = 0.66D;
    private static final double RAW_RETRIEVAL_SCORE = 0.55D;

    @Resource
    private StandardRagPromptBuilder standardRagPromptBuilder;

    @Resource
    private VectorStoreGateway vectorStoreGateway;

    @Resource
    private ModelGateway modelGateway;

    @Resource
    private AiDegradationService aiDegradationService;

    @Resource
    private StandardDocumentService standardDocumentService;

    @Resource
    private QcStandardClauseMapper standardClauseMapper;

    @Resource
    private QcQualityStandardMapper qualityStandardMapper;

    @Override
    public StandardRagAnswerVO query(StandardRagQueryCmd cmd) {
        validateQuery(cmd);
        List<Double> queryVector = buildQueryVector(cmd);
        VectorSearchResponse vectorResponse = vectorStoreGateway.searchClauses(buildVectorSearchRequest(cmd, queryVector));
        List<StandardRagSourceVO> sources = filterUnpublishedStandardSources(toSources(vectorResponse));
        if (sources.isEmpty()) {
            sources = filterUnpublishedStandardSources(fallbackDbSearch(cmd));
        }
        if (sources.isEmpty()) {
            return noEvidence(cmd, vectorResponse, queryVector);
        }
        if (!hasAuthoritativeEvidence(sources)) {
            return lowQualityReferences(cmd, sources, queryVector);
        }
        sources = CitationReferenceSupport.filterActionableRagSources(sources);
        if (looksLikePromptInjection(cmd.getQuery())) {
            return promptInjectionRawAnswer(cmd, sources, queryVector);
        }

        ModelChatResponse modelResponse = modelGateway.chat(buildChatRequest(cmd, sources));
        AiDegradationResultVO degradation = aiDegradationService.resolveAiOutput(buildDegradationRequest(
                cmd, modelResponse, sources));
        return buildAnswer(cmd, sources, degradation, queryVector);
    }

    private void validateQuery(StandardRagQueryCmd cmd) {
        if (cmd == null || !StringUtils.hasText(cmd.getQuery())) {
            throw new ServiceException(ApiResult.CODE_BAD_REQUEST, "查询问题不能为空");
        }
    }

    private List<Double> buildQueryVector(StandardRagQueryCmd cmd) {
        ModelEmbeddingResponse response = modelGateway.embed(ModelEmbeddingRequest.builder()
                .businessType("STANDARD_RAG_QUERY")
                .businessId(cmd.getQuery())
                .inputTexts(java.util.Collections.singletonList(cmd.getQuery()))
                .build());
        if (response == null || !response.isSuccess() || CollectionUtils.isEmpty(response.getEmbeddings())) {
            log.warn("标准RAG问题向量化失败，query={}, error={}",
                    cmd.getQuery(), response == null ? "empty response" : response.getErrorMessage());
            return null;
        }
        ModelEmbedding embedding = response.getEmbeddings().get(0);
        if (embedding == null || CollectionUtils.isEmpty(embedding.getVector())) {
            log.warn("标准RAG问题向量化结果为空，query={}", cmd.getQuery());
            return null;
        }
        return embedding.getVector();
    }

    private VectorSearchRequest buildVectorSearchRequest(StandardRagQueryCmd cmd, List<Double> queryVector) {
        return VectorSearchRequest.builder()
                .businessType("STANDARD_RAG")
                .queryText(cmd.getQuery())
                .queryVector(queryVector)
                .sourceTypes(cmd.getSourceTypes())
                .customerId(cmd.getCustomerId())
                .variety(cmd.getVariety())
                .grade(cmd.getGrade())
                .indicatorCode(cmd.getIndicatorCode())
                .effectiveDate(cmd.getEffectiveDate())
                .topK(safeTopK(cmd.getTopK()))
                .displayMinScore(0.30D)
                .answerMinScore(0.60D)
                .build();
    }

    private ModelChatRequest buildChatRequest(StandardRagQueryCmd cmd, List<StandardRagSourceVO> sources) {
        StandardRagPromptBuilder.BuiltStandardRagPrompt built = standardRagPromptBuilder.build(cmd, sources);
        log.info("标准RAG调用Chat模型，sourceCount={}, promptChars={}, sourceIds={}",
                sources.size(),
                built.getUserPromptLength(),
                sources.stream().map(StandardRagSourceVO::getClauseId).collect(Collectors.toList()));
        return built.getRequest();
    }

    private AiDegradationRequest buildDegradationRequest(StandardRagQueryCmd cmd, ModelChatResponse modelResponse,
                                                         List<StandardRagSourceVO> sources) {
        AiDegradationRequest request = new AiDegradationRequest();
        request.setAssessmentType("STANDARD_RAG");
        request.setBusinessType("STANDARD_RAG_QUERY");
        request.setGeneratedOutput(isGroundedGeneratedOutput(modelResponse, sources) ? modelResponse.getContent() : null);
        request.setRawReferences(CitationReferenceSupport.toAiReferences(sources));
        if (modelResponse != null && !modelResponse.isSuccess()) {
            request.setUnavailableReason(modelResponse.getErrorMessage());
        } else if (modelResponse != null && modelResponse.isSuccess() && request.getGeneratedOutput() == null) {
            request.setUnavailableReason("模型输出缺少可核验来源标记，已降级为原始检索条款");
        }
        request.setConfidenceLabel(modelResponse != null && modelResponse.isSuccess() ? "HIGH" : "MEDIUM");
        request.setConfidenceScore(modelResponse != null && modelResponse.isSuccess() ? HIGH_CONFIDENCE_SCORE : MEDIUM_CONFIDENCE_SCORE);
        return request;
    }

    private StandardRagAnswerVO buildAnswer(StandardRagQueryCmd cmd, List<StandardRagSourceVO> sources,
                                            AiDegradationResultVO degradation, List<Double> queryVector) {
        StandardRagAnswerVO answer = new StandardRagAnswerVO();
        answer.setQuery(cmd.getQuery());
        answer.setAnswer(degradation.getOutputText());
        answer.setRefused(false);
        answer.setConfidenceLabel(degradation.getConfidenceLabel());
        answer.setConfidenceScore(degradation.getConfidenceScore());
        answer.setDegradationSource(degradation.getDegradationSource());
        answer.setDegradationReason(degradation.getDegradationReason());
        answer.setCacheHit(AiDegradationSource.CACHE.getCode().equals(degradation.getDegradationSource()));
        answer.setEmbeddingUsed(!CollectionUtils.isEmpty(queryVector));
        answer.setRetrievalMode(!CollectionUtils.isEmpty(queryVector) ? "ES_VECTOR_SCRIPT_SCORE" : "ES_TEXT_OR_DB_FALLBACK");
        answer.setChatPromptSourceCount(sources.size());
        answer.setSources(sources);
        return answer;
    }

    private StandardRagAnswerVO noEvidence(StandardRagQueryCmd cmd, VectorSearchResponse vectorResponse,
                                           List<Double> queryVector) {
        StandardRagAnswerVO answer = new StandardRagAnswerVO();
        answer.setQuery(cmd.getQuery());
        answer.setAnswer("");
        answer.setRefused(true);
        answer.setRefusalReason(vectorResponse != null && !vectorResponse.isSuccess()
                ? "标准知识检索不可用，且未找到本地可引用条款"
                : "在已登记标准/协议/案例条款中未找到可引用依据");
        answer.setConfidenceLabel("LOW");
        answer.setConfidenceScore(0.20D);
        answer.setDegradationSource(AiDegradationSource.UNAVAILABLE.getCode());
        answer.setDegradationReason(answer.getRefusalReason());
        answer.setCacheHit(false);
        answer.setEmbeddingUsed(!CollectionUtils.isEmpty(queryVector));
        answer.setRetrievalMode(!CollectionUtils.isEmpty(queryVector) ? "ES_VECTOR_SCRIPT_SCORE" : "ES_TEXT_OR_DB_FALLBACK");
        answer.setChatPromptSourceCount(0);
        answer.setSources(new ArrayList<>());
        return answer;
    }

    private StandardRagAnswerVO lowQualityReferences(StandardRagQueryCmd cmd, List<StandardRagSourceVO> sources,
                                                     List<Double> queryVector) {
        StandardRagAnswerVO answer = new StandardRagAnswerVO();
        answer.setQuery(cmd.getQuery());
        answer.setAnswer("");
        answer.setRefused(true);
        answer.setRefusalReason("检索结果相关性不足，系统仅展示参考条款，不生成权威回答");
        answer.setConfidenceLabel("LOW");
        answer.setConfidenceScore(0.35D);
        answer.setDegradationSource(AiDegradationSource.RAW_RETRIEVAL.getCode());
        answer.setDegradationReason("来源条款未达到回答阈值");
        answer.setCacheHit(false);
        answer.setEmbeddingUsed(!CollectionUtils.isEmpty(queryVector));
        answer.setRetrievalMode(!CollectionUtils.isEmpty(queryVector) ? "ES_VECTOR_SCRIPT_SCORE" : "ES_TEXT_OR_DB_FALLBACK");
        answer.setChatPromptSourceCount(0);
        answer.setSources(sources);
        return answer;
    }

    private StandardRagAnswerVO promptInjectionRawAnswer(StandardRagQueryCmd cmd, List<StandardRagSourceVO> sources,
                                                         List<Double> queryVector) {
        StandardRagAnswerVO answer = new StandardRagAnswerVO();
        answer.setQuery(cmd.getQuery());
        answer.setAnswer("检测到可能要求忽略标准、编造依据或泄露系统提示的指令。系统已忽略该指令，仅展示检索到的来源条款。");
        answer.setRefused(false);
        answer.setConfidenceLabel("LOW");
        answer.setConfidenceScore(0.40D);
        answer.setDegradationSource(AiDegradationSource.RAW_RETRIEVAL.getCode());
        answer.setDegradationReason("提示注入安全保护，未调用模型生成");
        answer.setCacheHit(false);
        answer.setEmbeddingUsed(!CollectionUtils.isEmpty(queryVector));
        answer.setRetrievalMode(!CollectionUtils.isEmpty(queryVector) ? "ES_VECTOR_SCRIPT_SCORE" : "ES_TEXT_OR_DB_FALLBACK");
        answer.setChatPromptSourceCount(0);
        answer.setSources(sources);
        return answer;
    }

    private boolean hasAuthoritativeEvidence(List<StandardRagSourceVO> sources) {
        if (CollectionUtils.isEmpty(sources)) {
            return false;
        }
        for (StandardRagSourceVO source : sources) {
            if (!Boolean.TRUE.equals(source.getReferenceOnly())) {
                return true;
            }
        }
        return false;
    }

    private boolean looksLikePromptInjection(String query) {
        if (!StringUtils.hasText(query)) {
            return false;
        }
        String lower = query.toLowerCase();
        return lower.contains("ignore previous")
                || lower.contains("ignore all")
                || lower.contains("system prompt")
                || lower.contains("developer message")
                || lower.contains("jailbreak")
                || query.contains("忽略")
                || query.contains("无视")
                || query.contains("编造")
                || query.contains("硬编")
                || query.contains("绕过")
                || query.contains("泄露提示词")
                || query.contains("不要引用来源");
    }

    private boolean isGroundedGeneratedOutput(ModelChatResponse modelResponse, List<StandardRagSourceVO> sources) {
        if (modelResponse == null || !modelResponse.isSuccess()) {
            return false;
        }
        List<AiSourceReferenceVO> references = CitationReferenceSupport.toAiReferences(sources);
        return StringUtils.hasText(CitationReferenceSupport.acceptTrustedCitedOutput(
                modelResponse.getContent(), references, null));
    }

    private List<StandardRagSourceVO> toSources(VectorSearchResponse response) {
        if (response == null || !response.isSuccess() || CollectionUtils.isEmpty(response.getResults())) {
            return new ArrayList<>();
        }
        return response.getResults().stream().map(this::toSource).collect(Collectors.toList());
    }

    private StandardRagSourceVO toSource(VectorSearchResult result) {
        StandardRagSourceVO source = new StandardRagSourceVO();
        source.setClauseId(result.getClauseId());
        source.setDocumentId(result.getDocumentId());
        source.setSourceType(result.getSourceType());
        source.setStandardCode(result.getStandardCode());
        source.setStandardName(result.getStandardName());
        source.setVersionNo(result.getVersionNo());
        source.setClauseNo(result.getClauseNo());
        source.setPageNo(result.getPageNo());
        source.setParagraphText(result.getParagraphText());
        source.setSourceFileName(result.getSourceFileName());
        source.setScore(result.getScore());
        source.setReferenceOnly(result.getScore() != null && result.getScore() < 0.60D);
        return source;
    }

    private List<StandardRagSourceVO> fallbackDbSearch(StandardRagQueryCmd cmd) {
        StandardClausePageQuery query = new StandardClausePageQuery();
        query.setKeyword(cmd.getQuery());
        query.setCustomerId(cmd.getCustomerId());
        query.setVariety(cmd.getVariety());
        query.setGrade(cmd.getGrade());
        query.setIndicatorCode(cmd.getIndicatorCode());
        query.setPageNum(1);
        query.setPageSize(safeTopK(cmd.getTopK()));
        IPage<StandardClauseVO> page = standardDocumentService.pageClauses(query);
        List<StandardClauseVO> records = page.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            return new ArrayList<>();
        }
        Set<String> documentIds = records.stream()
                .map(StandardClauseVO::getDocumentId)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());
        Map<String, String> sourceFileNameByDocumentId = loadSourceFileNames(documentIds);
        return records.stream()
                .map(clause -> toSource(clause, sourceFileNameByDocumentId.get(clause.getDocumentId())))
                .collect(Collectors.toList());
    }

    private Map<String, String> loadSourceFileNames(Set<String> documentIds) {
        if (CollectionUtils.isEmpty(documentIds)) {
            return Collections.emptyMap();
        }
        return standardDocumentService.listDocumentsByIds(documentIds).stream()
                .filter(doc -> StringUtils.hasText(doc.getId()))
                .collect(Collectors.toMap(
                        doc -> doc.getId(),
                        doc -> doc.getSourceFileName() == null ? "" : doc.getSourceFileName(),
                        (left, right) -> left));
    }

    private StandardRagSourceVO toSource(StandardClauseVO clause, String sourceFileName) {
        StandardRagSourceVO source = new StandardRagSourceVO();
        source.setClauseId(clause.getId());
        source.setDocumentId(clause.getDocumentId());
        source.setSourceType(clause.getSourceType());
        source.setStandardCode(clause.getStandardCode());
        source.setStandardName(clause.getStandardName());
        source.setVersionNo(clause.getVersionNo());
        source.setClauseNo(clause.getClauseNo());
        source.setPageNo(clause.getPageNo());
        source.setParagraphText(clause.getParagraphText());
        source.setSourceFileName(sourceFileName);
        source.setScore(RAW_RETRIEVAL_SCORE);
        source.setReferenceOnly(true);
        return source;
    }

    private List<StandardRagSourceVO> filterUnpublishedStandardSources(List<StandardRagSourceVO> sources) {
        if (CollectionUtils.isEmpty(sources)) {
            return sources;
        }
        List<String> clauseIds = sources.stream()
                .map(StandardRagSourceVO::getClauseId)
                .filter(StringUtils::hasText)
                .collect(Collectors.toList());
        if (clauseIds.isEmpty()) {
            return sources;
        }
        List<QcStandardClause> clauses = standardClauseMapper.selectBatchIds(clauseIds);
        if (CollectionUtils.isEmpty(clauses)) {
            return sources;
        }
        Set<String> standardIds = clauses.stream()
                .map(QcStandardClause::getStandardId)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());
        if (standardIds.isEmpty()) {
            return sources;
        }
        List<QcQualityStandard> standards = qualityStandardMapper.selectBatchIds(standardIds);
        Set<String> blockedStandardIds = standards.stream()
                .filter(standard -> !"PUBLISHED".equals(standard.getStatus()))
                .map(QcQualityStandard::getId)
                .collect(Collectors.toSet());
        if (blockedStandardIds.isEmpty()) {
            return sources;
        }
        Map<String, String> clauseStandardMap = clauses.stream()
                .filter(clause -> StringUtils.hasText(clause.getStandardId()))
                .collect(Collectors.toMap(QcStandardClause::getId, QcStandardClause::getStandardId, (left, right) -> left));
        Set<String> blockedClauseIds = new HashSet<>();
        for (Map.Entry<String, String> entry : clauseStandardMap.entrySet()) {
            if (blockedStandardIds.contains(entry.getValue())) {
                blockedClauseIds.add(entry.getKey());
            }
        }
        return sources.stream()
                .filter(source -> !blockedClauseIds.contains(source.getClauseId()))
                .collect(Collectors.toList());
    }

    private int safeTopK(Integer topK) {
        if (topK == null || topK < 1) {
            return DEFAULT_TOP_K;
        }
        return Math.min(topK, 20);
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
