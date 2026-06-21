package com.jhict.quality.service.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jhict.quality.dto.PreparedClauseIndexCmd;
import com.jhict.quality.dto.StandardClausePageQuery;
import com.jhict.quality.dto.StandardDocumentPageQuery;
import com.jhict.quality.gateway.vector.VectorIndexResponse;
import com.jhict.quality.vo.StandardClauseVO;
import com.jhict.quality.vo.StandardDocumentVO;

import java.util.List;

/**
 * Service API for source standard documents and RAG clauses.
 */
public interface StandardDocumentService {

    /**
     * Page source documents for standard/agreement/case retrieval.
     *
     * @param query page query
     * @return document page
     */
    IPage<StandardDocumentVO> pageDocuments(StandardDocumentPageQuery query);

    /**
     * Get one source document by id.
     *
     * @param id source document id
     * @return document details
     */
    StandardDocumentVO getDocumentById(String id);

    /**
     * Page source clauses for RAG retrieval and citation display.
     *
     * @param query clause page query
     * @return clause page
     */
    IPage<StandardClauseVO> pageClauses(StandardClausePageQuery query);

    /**
     * Get one source clause by id.
     *
     * @param id clause id
     * @return clause details
     */
    StandardClauseVO getClauseById(String id);

    /**
     * List clauses by ids while preserving only existing rows.
     *
     * @param clauseIds clause ids
     * @return clause details
     */
    List<StandardClauseVO> listClausesByIds(List<String> clauseIds);

    /**
     * Index prepared clauses into the vector store for demo/RAG retrieval.
     *
     * @param cmd indexing command
     * @return vector indexing response
     */
    VectorIndexResponse indexPreparedClauses(PreparedClauseIndexCmd cmd);
}
