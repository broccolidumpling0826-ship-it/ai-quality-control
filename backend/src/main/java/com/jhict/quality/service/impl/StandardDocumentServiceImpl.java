package com.jhict.quality.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jhict.quality.common.entity.ApiResult;
import com.jhict.quality.common.exception.ServiceException;
import com.jhict.quality.dto.PreparedClauseIndexCmd;
import com.jhict.quality.dto.StandardClausePageQuery;
import com.jhict.quality.dto.StandardDocumentPageQuery;
import com.jhict.quality.entity.QcStandardClause;
import com.jhict.quality.entity.QcStandardDocument;
import com.jhict.quality.gateway.vector.VectorClauseDocument;
import com.jhict.quality.gateway.vector.VectorIndexRequest;
import com.jhict.quality.gateway.vector.VectorIndexResponse;
import com.jhict.quality.gateway.vector.VectorStoreGateway;
import com.jhict.quality.mapper.QcStandardClauseMapper;
import com.jhict.quality.mapper.QcStandardDocumentMapper;
import com.jhict.quality.service.api.StandardDocumentService;
import com.jhict.quality.vo.StandardClauseVO;
import com.jhict.quality.vo.StandardDocumentVO;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
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
        VectorIndexRequest request = VectorIndexRequest.builder()
                .businessType("STANDARD_CLAUSE")
                .businessId(safeCmd.getDocumentId())
                .indexName(safeCmd.getIndexName())
                .clauses(clauses.stream().map(this::toVectorClauseDocument).collect(Collectors.toList()))
                .build();
        VectorIndexResponse response = vectorStoreGateway.indexClauses(request);
        if (response.isSuccess() || !CollectionUtils.isEmpty(response.getFailedClauseIds())) {
            updateEmbeddingStatus(clauses, response.getFailedClauseIds());
        }
        return response;
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

    private VectorClauseDocument toVectorClauseDocument(QcStandardClause clause) {
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
                .build();
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
