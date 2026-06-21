package com.jhict.quality.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jhict.quality.common.entity.ApiResult;
import com.jhict.quality.common.exception.ServiceException;
import com.jhict.quality.dto.PreparedClauseIndexCmd;
import com.jhict.quality.dto.StandardClausePageQuery;
import com.jhict.quality.dto.StandardDocumentIngestCmd;
import com.jhict.quality.dto.StandardDocumentPageQuery;
import com.jhict.quality.entity.QcQualityStandard;
import com.jhict.quality.entity.QcStandardClause;
import com.jhict.quality.entity.QcStandardDocument;
import com.jhict.quality.gateway.model.ModelEmbedding;
import com.jhict.quality.gateway.model.ModelEmbeddingRequest;
import com.jhict.quality.gateway.model.ModelEmbeddingResponse;
import com.jhict.quality.gateway.model.ModelGateway;
import com.jhict.quality.gateway.vector.VectorClauseDocument;
import com.jhict.quality.gateway.vector.VectorDeleteRequest;
import com.jhict.quality.gateway.vector.VectorIndexRequest;
import com.jhict.quality.gateway.vector.VectorIndexResponse;
import com.jhict.quality.gateway.vector.VectorStoreGateway;
import com.jhict.quality.gateway.vector.elasticsearch.ElasticsearchVectorProperties;
import com.jhict.quality.mapper.QcStandardClauseMapper;
import com.jhict.quality.mapper.QcStandardDocumentMapper;
import com.jhict.quality.service.api.StandardDocumentService;
import com.jhict.quality.service.support.rag.ExtractedStandardDocument;
import com.jhict.quality.service.support.rag.StandardClauseChunk;
import com.jhict.quality.service.support.rag.LegacyStandardDocumentFields;
import com.jhict.quality.service.support.rag.StandardClauseChunker;
import com.jhict.quality.service.support.rag.StandardDocumentTextExtractor;
import com.jhict.quality.service.support.rag.StandardSourceFileStorageService;
import com.jhict.quality.vo.StandardClauseVO;
import com.jhict.quality.vo.StandardDocumentIngestVO;
import com.jhict.quality.vo.StandardDocumentVO;
import com.jhict.quality.vo.StandardSourceDocumentVO;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class StandardDocumentServiceImpl implements StandardDocumentService {

    @Resource
    private QcStandardDocumentMapper standardDocumentMapper;

    @Resource
    private QcStandardClauseMapper standardClauseMapper;

    @Resource
    private VectorStoreGateway vectorStoreGateway;

    @Resource
    private ModelGateway modelGateway;

    @Resource
    private StandardDocumentTextExtractor standardDocumentTextExtractor;

    @Resource
    private StandardClauseChunker standardClauseChunker;

    @Resource
    private StandardSourceFileStorageService standardSourceFileStorageService;

    @Resource
    private ElasticsearchVectorProperties elasticsearchVectorProperties;

    @Override
    public IPage<StandardDocumentVO> pageDocuments(StandardDocumentPageQuery query) {
        StandardDocumentPageQuery safeQuery = query == null ? new StandardDocumentPageQuery() : query;
        Page<QcStandardDocument> pageParam = new Page<>(safePageNum(safeQuery.getPageNum()), safePageSize(safeQuery.getPageSize()));
        LambdaQueryWrapper<QcStandardDocument> wrapper = buildDocumentWrapper(safeQuery);
        IPage<QcStandardDocument> entityPage = standardDocumentMapper.selectPage(pageParam, wrapper);
        Page<StandardDocumentVO> voPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        voPage.setRecords(entityPage.getRecords().stream().map(this::toDocumentVO).collect(Collectors.toList()));
        return voPage;
    }

    @Override
    public StandardDocumentVO getDocumentById(String id) {
        if (!StringUtils.hasText(id)) {
            throw new ServiceException(ApiResult.CODE_BAD_REQUEST, "文档ID不能为空");
        }
        QcStandardDocument document = standardDocumentMapper.selectById(id);
        if (document == null) {
            throw new ServiceException(ApiResult.CODE_NOT_FOUND, "标准源文档不存在");
        }
        return toDocumentVO(document);
    }

    @Override
    public IPage<StandardClauseVO> pageClauses(StandardClausePageQuery query) {
        StandardClausePageQuery safeQuery = query == null ? new StandardClausePageQuery() : query;
        Page<QcStandardClause> pageParam = new Page<>(safePageNum(safeQuery.getPageNum()), safePageSize(safeQuery.getPageSize()));
        LambdaQueryWrapper<QcStandardClause> wrapper = buildClauseWrapper(safeQuery);
        IPage<QcStandardClause> entityPage = standardClauseMapper.selectPage(pageParam, wrapper);
        Page<StandardClauseVO> voPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        voPage.setRecords(entityPage.getRecords().stream().map(this::toClauseVO).collect(Collectors.toList()));
        return voPage;
    }

    @Override
    public StandardClauseVO getClauseById(String id) {
        if (!StringUtils.hasText(id)) {
            throw new ServiceException(ApiResult.CODE_BAD_REQUEST, "条款ID不能为空");
        }
        QcStandardClause clause = standardClauseMapper.selectById(id);
        if (clause == null) {
            throw new ServiceException(ApiResult.CODE_NOT_FOUND, "标准源条款不存在");
        }
        return toClauseVO(clause);
    }

    @Override
    public List<StandardClauseVO> listClausesByIds(List<String> clauseIds) {
        if (CollectionUtils.isEmpty(clauseIds)) {
            return Collections.emptyList();
        }
        return standardClauseMapper.selectBatchIds(clauseIds).stream()
                .map(this::toClauseVO)
                .collect(Collectors.toList());
    }

    @Override
    public VectorIndexResponse indexPreparedClauses(PreparedClauseIndexCmd cmd) {
        PreparedClauseIndexCmd safeCmd = cmd == null ? new PreparedClauseIndexCmd() : cmd;
        List<QcStandardClause> clauses = standardClauseMapper.selectList(buildPreparedIndexWrapper(safeCmd));
        if (clauses.isEmpty()) {
            return VectorIndexResponse.builder()
                    .success(true)
                    .provider(vectorStoreGateway.provider())
                    .indexName(safeCmd.getIndexName())
                    .indexedCount(0)
                    .failedClauseIds(Collections.emptyList())
                    .build();
        }
        ModelEmbeddingResponse embeddingResponse = modelGateway.embed(ModelEmbeddingRequest.builder()
                .businessType("STANDARD_CLAUSE_INDEX")
                .businessId(safeCmd.getDocumentId())
                .inputTexts(clauses.stream().map(this::embeddingText).collect(Collectors.toList()))
                .build());
        if (embeddingResponse == null || !embeddingResponse.isSuccess()
                || CollectionUtils.isEmpty(embeddingResponse.getEmbeddings())) {
            updateEmbeddingFailed(clauses);
            return VectorIndexResponse.builder()
                    .success(false)
                    .provider(vectorStoreGateway.provider())
                    .indexName(safeCmd.getIndexName())
                    .indexedCount(0)
                    .failedClauseIds(clauses.stream().map(QcStandardClause::getId).collect(Collectors.toList()))
                    .errorCategory(embeddingResponse == null ? "EMBEDDING_ERROR" : embeddingResponse.getErrorCategory())
                    .errorMessage(embeddingResponse == null ? "向量模型未返回结果" : embeddingResponse.getErrorMessage())
                    .build();
        }
        Map<Integer, List<Double>> embeddingByIndex = embeddingByIndex(embeddingResponse.getEmbeddings());
        if (!hasAllEmbeddings(clauses, embeddingByIndex)) {
            updateEmbeddingFailed(clauses);
            return VectorIndexResponse.builder()
                    .success(false)
                    .provider(vectorStoreGateway.provider())
                    .indexName(safeCmd.getIndexName())
                    .indexedCount(0)
                    .failedClauseIds(clauses.stream().map(QcStandardClause::getId).collect(Collectors.toList()))
                    .errorCategory("EMBEDDING_MISMATCH")
                    .errorMessage("向量模型返回数量与待索引条款不一致")
                    .build();
        }

        VectorIndexRequest request = VectorIndexRequest.builder()
                .businessType("STANDARD_CLAUSE")
                .businessId(safeCmd.getDocumentId())
                .indexName(safeCmd.getIndexName())
                .clauses(toVectorClauseDocuments(clauses, embeddingByIndex))
                .build();
        VectorIndexResponse response = vectorStoreGateway.indexClauses(request);
        if (response.isSuccess() || !CollectionUtils.isEmpty(response.getFailedClauseIds())) {
            updateEmbeddingStatus(clauses, response.getFailedClauseIds());
        }
        return response;
    }

    @Override
    public StandardDocumentIngestVO ingestAndIndexDocument(StandardDocumentIngestCmd cmd) {
        StandardDocumentIngestCmd safeCmd = cmd == null ? new StandardDocumentIngestCmd() : cmd;
        if (!StringUtils.hasText(safeCmd.getDocumentId())) {
            throw new ServiceException(ApiResult.CODE_BAD_REQUEST, "源文档ID不能为空");
        }
        QcStandardDocument document = standardDocumentMapper.selectById(safeCmd.getDocumentId());
        if (document == null) {
            throw new ServiceException(ApiResult.CODE_NOT_FOUND, "标准源文档不存在");
        }
        try {
            if (Boolean.TRUE.equals(safeCmd.getReindexExisting())) {
                deleteExistingClauses(document.getId(), safeCmd.getIndexName());
            }
            ExtractedStandardDocument extracted = standardDocumentTextExtractor.extract(
                    document.getSourceFileName(), document.getSourceFilePath());
            List<StandardClauseChunk> chunks = standardClauseChunker.chunk(extracted, safeCmd.getMaxChunkChars());
            if (CollectionUtils.isEmpty(chunks)) {
                updateDocumentStatus(document.getId(), "FAILED", "FAILED", "未从源文档提取到可索引文本");
                return buildIngestVO(document.getId(), "FAILED", "FAILED", extracted.getSegments().size(),
                        0, null, "PARSE_EMPTY", "未从源文档提取到可索引文本");
            }
            List<QcStandardClause> clauses = insertChunks(document, chunks);
            updateDocumentStatus(document.getId(), "PARSED", "PENDING", null);

            PreparedClauseIndexCmd indexCmd = new PreparedClauseIndexCmd();
            indexCmd.setDocumentId(document.getId());
            indexCmd.setIndexName(safeCmd.getIndexName());
            indexCmd.setOnlyPending(Boolean.TRUE);
            VectorIndexResponse indexResponse = indexPreparedClauses(indexCmd);
            boolean success = indexResponse != null && indexResponse.isSuccess();
            updateDocumentStatus(document.getId(), "PARSED", success ? "INDEXED" : "FAILED",
                    success ? null : safeErrorMessage(indexResponse));

            StandardDocumentIngestVO vo = buildIngestVO(document.getId(), "PARSED",
                    success ? "INDEXED" : "FAILED", extracted.getSegments().size(), clauses.size(),
                    indexResponse, success ? null : safeErrorCategory(indexResponse),
                    success ? null : safeErrorMessage(indexResponse));
            vo.setRetrievalMode("EMBEDDING_VECTOR");
            return vo;
        } catch (ServiceException ex) {
            updateDocumentStatus(document.getId(), "FAILED", "FAILED", ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            updateDocumentStatus(document.getId(), "FAILED", "FAILED", ex.getMessage());
            throw new ServiceException(ApiResult.CODE_SERVER_ERROR, "标准文档入库失败: " + ex.getMessage());
        }
    }

    @Override
    public QcStandardDocument getLinkedDocument(String standardId) {
        if (!StringUtils.hasText(standardId)) {
            return null;
        }
        return standardDocumentMapper.selectOne(new LambdaQueryWrapper<QcStandardDocument>()
                .eq(QcStandardDocument::getStandardId, standardId)
                .eq(QcStandardDocument::getStatus, "ACTIVE")
                .last("LIMIT 1"));
    }

    @Override
    public QcStandardDocument syncLinkedDocument(QcQualityStandard standard) {
        if (standard == null || !StringUtils.hasText(standard.getId())) {
            throw new ServiceException(ApiResult.CODE_BAD_REQUEST, "标准信息不完整，无法同步源文档");
        }
        QcStandardDocument existing = getLinkedDocument(standard.getId());
        if (existing == null) {
            QcStandardDocument document = buildLinkedDocumentEntity(standard);
            document.setParseStatus("PENDING");
            document.setIndexStatus("PENDING");
            document.setStatus("ACTIVE");
            standardDocumentMapper.insert(document);
            return document;
        }
        applyStandardSnapshot(existing.getId(), standard);
        return standardDocumentMapper.selectById(existing.getId());
    }

    @Override
    public void removeLinkedDocument(String standardId) {
        QcStandardDocument document = getLinkedDocument(standardId);
        if (document != null) {
            purgeLinkedVectors(standardId);
            standardDocumentMapper.deleteById(document.getId());
        }
        standardSourceFileStorageService.deleteStandardDirectory(standardId);
    }

    @Override
    public void purgeLinkedVectors(String standardId) {
        QcStandardDocument document = getLinkedDocument(standardId);
        if (document == null) {
            return;
        }
        deleteExistingClauses(document.getId(), defaultIndexName());
        QcStandardDocument update = new QcStandardDocument();
        update.setId(document.getId());
        update.setIndexStatus("PENDING");
        update.setIndexedAt(null);
        update.setParseErrorMessage(null);
        standardDocumentMapper.updateById(update);
    }

    @Override
    public StandardDocumentIngestVO ingestLinkedDocument(String standardId) {
        QcStandardDocument document = getLinkedDocument(standardId);
        if (document == null || !StringUtils.hasText(document.getSourceFilePath())) {
            throw new ServiceException(ApiResult.CODE_BAD_REQUEST, "标准未上传源 PDF，无法索引");
        }
        StandardDocumentIngestCmd cmd = new StandardDocumentIngestCmd();
        cmd.setDocumentId(document.getId());
        cmd.setReindexExisting(Boolean.TRUE);
        cmd.setIndexName(defaultIndexName());
        return ingestAndIndexDocument(cmd);
    }

    @Override
    public StandardSourceDocumentVO buildSourceDocumentSummary(String standardId) {
        QcStandardDocument document = getLinkedDocument(standardId);
        StandardSourceDocumentVO vo = new StandardSourceDocumentVO();
        if (document == null) {
            vo.setHasSourceFile(false);
            return vo;
        }
        vo.setDocumentId(document.getId());
        vo.setSourceFileName(document.getSourceFileName());
        vo.setParseStatus(document.getParseStatus());
        vo.setIndexStatus(document.getIndexStatus());
        vo.setIndexedAt(document.getIndexedAt());
        vo.setParseErrorMessage(document.getParseErrorMessage());
        vo.setHasSourceFile(StringUtils.hasText(document.getSourceFilePath()));
        long chunkCount = standardClauseMapper.selectCount(new LambdaQueryWrapper<QcStandardClause>()
                .eq(QcStandardClause::getDocumentId, document.getId())
                .eq(QcStandardClause::getStatus, "ACTIVE"));
        vo.setChunkCount((int) chunkCount);
        return vo;
    }

    private QcStandardDocument buildLinkedDocumentEntity(QcQualityStandard standard) {
        QcStandardDocument document = new QcStandardDocument();
        document.setStandardId(standard.getId());
        document.setDocumentCode(standard.getStandardCode());
        document.setDocumentName(standard.getStandardName());
        document.setDocumentType("STANDARD");
        document.setStandardType(standard.getStandardType());
        document.setStandardCode(standard.getStandardCode());
        document.setStandardName(standard.getStandardName());
        document.setVersionNo(standard.getVersionNo());
        document.setCustomerId(standard.getCustomerId());
        document.setVariety(standard.getVariety());
        document.setGrade(standard.getGrade());
        document.setSpecRange(standard.getSpecRange());
        document.setEffectiveDate(standard.getEffectiveDate());
        document.setExpiryDate(standard.getExpiryDate());
        LegacyStandardDocumentFields.applyPendingFileFields(document, standard.getStandardCode());
        return document;
    }

    private void applyStandardSnapshot(String documentId, QcQualityStandard standard) {
        QcStandardDocument update = new QcStandardDocument();
        update.setId(documentId);
        update.setDocumentCode(standard.getStandardCode());
        update.setDocumentName(standard.getStandardName());
        update.setStandardType(standard.getStandardType());
        update.setStandardCode(standard.getStandardCode());
        update.setStandardName(standard.getStandardName());
        update.setVersionNo(standard.getVersionNo());
        update.setCustomerId(standard.getCustomerId());
        update.setVariety(standard.getVariety());
        update.setGrade(standard.getGrade());
        update.setSpecRange(standard.getSpecRange());
        update.setEffectiveDate(standard.getEffectiveDate());
        update.setExpiryDate(standard.getExpiryDate());
        standardDocumentMapper.updateById(update);
    }

    private String defaultIndexName() {
        return elasticsearchVectorProperties.getStandardIndex();
    }

    private LambdaQueryWrapper<QcStandardDocument> buildDocumentWrapper(StandardDocumentPageQuery query) {
        LambdaQueryWrapper<QcStandardDocument> wrapper = new LambdaQueryWrapper<QcStandardDocument>()
                .eq(StringUtils.hasText(query.getDocumentType()), QcStandardDocument::getDocumentType, query.getDocumentType())
                .eq(StringUtils.hasText(query.getStandardType()), QcStandardDocument::getStandardType, query.getStandardType())
                .eq(StringUtils.hasText(query.getCustomerId()), QcStandardDocument::getCustomerId, query.getCustomerId())
                .eq(StringUtils.hasText(query.getVariety()), QcStandardDocument::getVariety, query.getVariety())
                .eq(StringUtils.hasText(query.getGrade()), QcStandardDocument::getGrade, query.getGrade())
                .eq(StringUtils.hasText(query.getParseStatus()), QcStandardDocument::getParseStatus, query.getParseStatus())
                .eq(StringUtils.hasText(query.getIndexStatus()), QcStandardDocument::getIndexStatus, query.getIndexStatus())
                .eq(StringUtils.hasText(query.getStatus()), QcStandardDocument::getStatus, query.getStatus())
                .orderByDesc(QcStandardDocument::getCreateDateTime);
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(QcStandardDocument::getDocumentCode, query.getKeyword())
                    .or().like(QcStandardDocument::getDocumentName, query.getKeyword())
                    .or().like(QcStandardDocument::getStandardCode, query.getKeyword())
                    .or().like(QcStandardDocument::getStandardName, query.getKeyword()));
        }
        return wrapper;
    }

    private void deleteExistingClauses(String documentId, String indexName) {
        List<QcStandardClause> existingClauses = standardClauseMapper.selectList(
                new LambdaQueryWrapper<QcStandardClause>().eq(QcStandardClause::getDocumentId, documentId));
        for (QcStandardClause clause : existingClauses) {
            vectorStoreGateway.deleteClause(VectorDeleteRequest.builder()
                    .indexName(indexName)
                    .clauseId(clause.getId())
                    .esDocumentKey(clause.getEsDocumentKey())
                    .build());
        }
        if (!existingClauses.isEmpty()) {
            standardClauseMapper.delete(new LambdaQueryWrapper<QcStandardClause>()
                    .eq(QcStandardClause::getDocumentId, documentId));
        }
    }

    private List<QcStandardClause> insertChunks(QcStandardDocument document, List<StandardClauseChunk> chunks) {
        List<QcStandardClause> clauses = new ArrayList<>();
        for (int i = 0; i < chunks.size(); i++) {
            StandardClauseChunk chunk = chunks.get(i);
            QcStandardClause clause = new QcStandardClause();
            clause.setDocumentId(document.getId());
            clause.setStandardId(document.getStandardId());
            clause.setClauseKey(document.getId() + ":" + chunk.getClauseNo() + ":" + (i + 1));
            clause.setClauseNo(chunk.getClauseNo());
            clause.setPageNo(chunk.getPageNo());
            clause.setParagraphText(chunk.getParagraphText());
            clause.setSourceType(StringUtils.hasText(document.getStandardType())
                    ? document.getStandardType() : document.getDocumentType());
            clause.setStandardType(document.getStandardType());
            clause.setStandardCode(document.getStandardCode());
            clause.setStandardName(document.getStandardName());
            clause.setVersionNo(document.getVersionNo());
            clause.setCustomerId(document.getCustomerId());
            clause.setVariety(document.getVariety());
            clause.setGrade(document.getGrade());
            clause.setSpecRange(document.getSpecRange());
            clause.setUsageScope(document.getUsageScope());
            clause.setEffectiveDate(document.getEffectiveDate());
            clause.setExpiryDate(document.getExpiryDate());
            clause.setRetrievalKeywords(chunk.getRetrievalKeywords());
            clause.setEmbeddingStatus("PENDING");
            clause.setRelevanceGroup("INGESTED");
            clause.setStatus("ACTIVE");
            standardClauseMapper.insert(clause);
            clauses.add(clause);
        }
        return clauses;
    }

    private void updateDocumentStatus(String documentId, String parseStatus, String indexStatus, String errorMessage) {
        QcStandardDocument update = new QcStandardDocument();
        update.setId(documentId);
        update.setParseStatus(parseStatus);
        update.setIndexStatus(indexStatus);
        update.setParseErrorMessage(errorMessage);
        if ("INDEXED".equals(indexStatus)) {
            update.setIndexedAt(LocalDateTime.now());
        }
        standardDocumentMapper.updateById(update);
    }

    private StandardDocumentIngestVO buildIngestVO(String documentId, String parseStatus, String indexStatus,
                                                   int pageCount, int chunkCount, VectorIndexResponse response,
                                                   String errorCategory, String errorMessage) {
        StandardDocumentIngestVO vo = new StandardDocumentIngestVO();
        vo.setDocumentId(documentId);
        vo.setParseStatus(parseStatus);
        vo.setIndexStatus(indexStatus);
        vo.setExtractedPageCount(pageCount);
        vo.setChunkCount(chunkCount);
        vo.setIndexedCount(response == null || response.getIndexedCount() == null ? 0 : response.getIndexedCount());
        vo.setFailedClauseIds(response == null || response.getFailedClauseIds() == null
                ? Collections.emptyList() : response.getFailedClauseIds());
        vo.setErrorCategory(errorCategory);
        vo.setErrorMessage(errorMessage);
        return vo;
    }

    private String safeErrorCategory(VectorIndexResponse response) {
        return response == null ? "INDEX_ERROR" : response.getErrorCategory();
    }

    private String safeErrorMessage(VectorIndexResponse response) {
        if (response == null) {
            return "向量索引未返回结果";
        }
        return response.getErrorMessage();
    }

    private LambdaQueryWrapper<QcStandardClause> buildClauseWrapper(StandardClausePageQuery query) {
        LambdaQueryWrapper<QcStandardClause> wrapper = new LambdaQueryWrapper<QcStandardClause>()
                .eq(StringUtils.hasText(query.getDocumentId()), QcStandardClause::getDocumentId, query.getDocumentId())
                .eq(StringUtils.hasText(query.getStandardId()), QcStandardClause::getStandardId, query.getStandardId())
                .eq(StringUtils.hasText(query.getSourceType()), QcStandardClause::getSourceType, query.getSourceType())
                .eq(StringUtils.hasText(query.getCustomerId()), QcStandardClause::getCustomerId, query.getCustomerId())
                .eq(StringUtils.hasText(query.getVariety()), QcStandardClause::getVariety, query.getVariety())
                .eq(StringUtils.hasText(query.getGrade()), QcStandardClause::getGrade, query.getGrade())
                .eq(StringUtils.hasText(query.getIndicatorCode()), QcStandardClause::getIndicatorCode, query.getIndicatorCode())
                .eq(StringUtils.hasText(query.getEmbeddingStatus()), QcStandardClause::getEmbeddingStatus, query.getEmbeddingStatus())
                .eq(QcStandardClause::getStatus, "ACTIVE")
                .orderByAsc(QcStandardClause::getStandardCode)
                .orderByAsc(QcStandardClause::getClauseNo);
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(QcStandardClause::getClauseNo, query.getKeyword())
                    .or().like(QcStandardClause::getParagraphText, query.getKeyword())
                    .or().like(QcStandardClause::getStandardCode, query.getKeyword())
                    .or().like(QcStandardClause::getIndicatorName, query.getKeyword())
                    .or().like(QcStandardClause::getIndicatorCode, query.getKeyword()));
        }
        return wrapper;
    }

    private LambdaQueryWrapper<QcStandardClause> buildPreparedIndexWrapper(PreparedClauseIndexCmd cmd) {
        LambdaQueryWrapper<QcStandardClause> wrapper = new LambdaQueryWrapper<QcStandardClause>()
                .eq(QcStandardClause::getStatus, "ACTIVE")
                .eq(StringUtils.hasText(cmd.getDocumentId()), QcStandardClause::getDocumentId, cmd.getDocumentId())
                .eq(StringUtils.hasText(cmd.getRelevanceGroup()), QcStandardClause::getRelevanceGroup, cmd.getRelevanceGroup())
                .orderByAsc(QcStandardClause::getDocumentId)
                .orderByAsc(QcStandardClause::getClauseNo);
        if (Boolean.TRUE.equals(cmd.getOnlyPending())) {
            wrapper.ne(QcStandardClause::getEmbeddingStatus, "INDEXED");
        }
        return wrapper;
    }

    private List<VectorClauseDocument> toVectorClauseDocuments(List<QcStandardClause> clauses,
                                                               Map<Integer, List<Double>> embeddingByIndex) {
        return java.util.stream.IntStream.range(0, clauses.size())
                .mapToObj(i -> toVectorClauseDocument(clauses.get(i), embeddingByIndex.get(i)))
                .collect(Collectors.toList());
    }

    private Map<Integer, List<Double>> embeddingByIndex(List<ModelEmbedding> embeddings) {
        Map<Integer, List<Double>> result = new HashMap<>();
        for (int i = 0; i < embeddings.size(); i++) {
            ModelEmbedding embedding = embeddings.get(i);
            if (embedding != null && !CollectionUtils.isEmpty(embedding.getVector())) {
                result.put(embedding.getIndex() == null ? i : embedding.getIndex(), embedding.getVector());
            }
        }
        return result;
    }

    private boolean hasAllEmbeddings(List<QcStandardClause> clauses, Map<Integer, List<Double>> embeddingByIndex) {
        if (clauses == null || embeddingByIndex == null || clauses.size() != embeddingByIndex.size()) {
            return false;
        }
        for (int i = 0; i < clauses.size(); i++) {
            if (CollectionUtils.isEmpty(embeddingByIndex.get(i))) {
                return false;
            }
        }
        return true;
    }

    private String embeddingText(QcStandardClause clause) {
        return String.join("\n",
                nullToEmpty(clause.getStandardCode()),
                nullToEmpty(clause.getStandardName()),
                nullToEmpty(clause.getClauseNo()),
                nullToEmpty(clause.getIndicatorName()),
                nullToEmpty(clause.getRetrievalKeywords()),
                nullToEmpty(clause.getParagraphText()));
    }

    private VectorClauseDocument toVectorClauseDocument(QcStandardClause clause, List<Double> embedding) {
        return VectorClauseDocument.builder()
                .clauseId(clause.getId())
                .documentId(clause.getDocumentId())
                .clauseKey(clause.getClauseKey())
                .clauseNo(clause.getClauseNo())
                .pageNo(clause.getPageNo())
                .paragraphText(clause.getParagraphText())
                .sourceType(clause.getSourceType())
                .standardType(clause.getStandardType())
                .standardCode(clause.getStandardCode())
                .standardName(clause.getStandardName())
                .versionNo(clause.getVersionNo())
                .customerId(clause.getCustomerId())
                .variety(clause.getVariety())
                .grade(clause.getGrade())
                .specRange(clause.getSpecRange())
                .usageScope(clause.getUsageScope())
                .indicatorId(clause.getIndicatorId())
                .indicatorCode(clause.getIndicatorCode())
                .indicatorName(clause.getIndicatorName())
                .effectiveDate(clause.getEffectiveDate() == null ? null : clause.getEffectiveDate().toString())
                .expiryDate(clause.getExpiryDate() == null ? null : clause.getExpiryDate().toString())
                .retrievalKeywords(clause.getRetrievalKeywords())
                .embedding(embedding)
                .build();
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private void updateEmbeddingStatus(List<QcStandardClause> clauses, List<String> failedClauseIds) {
        Set<String> failedSet = CollectionUtils.isEmpty(failedClauseIds)
                ? Collections.emptySet()
                : new HashSet<>(failedClauseIds);
        for (QcStandardClause clause : clauses) {
            QcStandardClause update = new QcStandardClause();
            update.setId(clause.getId());
            update.setEmbeddingStatus(failedSet.contains(clause.getId()) ? "FAILED" : "INDEXED");
            standardClauseMapper.updateById(update);
        }
    }

    private void updateEmbeddingFailed(List<QcStandardClause> clauses) {
        for (QcStandardClause clause : clauses) {
            QcStandardClause update = new QcStandardClause();
            update.setId(clause.getId());
            update.setEmbeddingStatus("FAILED");
            standardClauseMapper.updateById(update);
        }
    }

    private int safePageNum(Integer pageNum) {
        return pageNum == null || pageNum < 1 ? 1 : pageNum;
    }

    private int safePageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) {
            return 20;
        }
        return Math.min(pageSize, 200);
    }

    private StandardDocumentVO toDocumentVO(QcStandardDocument entity) {
        StandardDocumentVO vo = new StandardDocumentVO();
        vo.setId(entity.getId());
        vo.setStandardId(entity.getStandardId());
        vo.setDocumentCode(entity.getDocumentCode());
        vo.setDocumentName(entity.getDocumentName());
        vo.setDocumentType(entity.getDocumentType());
        vo.setStandardType(entity.getStandardType());
        vo.setStandardCode(entity.getStandardCode());
        vo.setStandardName(entity.getStandardName());
        vo.setVersionNo(entity.getVersionNo());
        vo.setCustomerId(entity.getCustomerId());
        vo.setVariety(entity.getVariety());
        vo.setGrade(entity.getGrade());
        vo.setSpecRange(entity.getSpecRange());
        vo.setUsageScope(entity.getUsageScope());
        vo.setEffectiveDate(entity.getEffectiveDate());
        vo.setExpiryDate(entity.getExpiryDate());
        vo.setSourceFileName(entity.getSourceFileName());
        vo.setSourceFilePath(entity.getSourceFilePath());
        vo.setParseStatus(entity.getParseStatus());
        vo.setIndexStatus(entity.getIndexStatus());
        vo.setParseErrorMessage(entity.getParseErrorMessage());
        vo.setIndexedAt(entity.getIndexedAt());
        vo.setStatus(entity.getStatus());
        vo.setRemark(entity.getRemark());
        return vo;
    }

    private StandardClauseVO toClauseVO(QcStandardClause entity) {
        StandardClauseVO vo = new StandardClauseVO();
        vo.setId(entity.getId());
        vo.setDocumentId(entity.getDocumentId());
        vo.setStandardId(entity.getStandardId());
        vo.setClauseKey(entity.getClauseKey());
        vo.setClauseNo(entity.getClauseNo());
        vo.setPageNo(entity.getPageNo());
        vo.setParagraphText(entity.getParagraphText());
        vo.setSourceType(entity.getSourceType());
        vo.setStandardType(entity.getStandardType());
        vo.setStandardCode(entity.getStandardCode());
        vo.setStandardName(entity.getStandardName());
        vo.setVersionNo(entity.getVersionNo());
        vo.setCustomerId(entity.getCustomerId());
        vo.setVariety(entity.getVariety());
        vo.setGrade(entity.getGrade());
        vo.setSpecRange(entity.getSpecRange());
        vo.setUsageScope(entity.getUsageScope());
        vo.setIndicatorId(entity.getIndicatorId());
        vo.setIndicatorCode(entity.getIndicatorCode());
        vo.setIndicatorName(entity.getIndicatorName());
        vo.setEffectiveDate(entity.getEffectiveDate());
        vo.setExpiryDate(entity.getExpiryDate());
        vo.setRetrievalKeywords(entity.getRetrievalKeywords());
        vo.setEsDocumentKey(entity.getEsDocumentKey());
        vo.setEmbeddingStatus(entity.getEmbeddingStatus());
        vo.setRelevanceGroup(entity.getRelevanceGroup());
        vo.setStatus(entity.getStatus());
        return vo;
    }
}
