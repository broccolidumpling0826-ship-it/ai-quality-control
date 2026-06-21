package com.jhict.quality.service.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jhict.quality.dto.PreparedClauseIndexCmd;
import com.jhict.quality.dto.StandardClausePageQuery;
import com.jhict.quality.dto.StandardDocumentIngestCmd;
import com.jhict.quality.dto.StandardDocumentPageQuery;
import com.jhict.quality.entity.QcStandardDocument;
import com.jhict.quality.entity.QcQualityStandard;
import com.jhict.quality.gateway.vector.VectorIndexResponse;
import com.jhict.quality.vo.StandardClauseVO;
import com.jhict.quality.vo.StandardDocumentIngestVO;
import com.jhict.quality.vo.StandardDocumentVO;
import com.jhict.quality.vo.StandardSourceDocumentVO;

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

    /**
     * Extract, chunk, embed, and index one source document.
     *
     * @param cmd ingestion command
     * @return ingestion summary
     */
    StandardDocumentIngestVO ingestAndIndexDocument(StandardDocumentIngestCmd cmd);

    /**
     * Find linked source document by structured standard id.
     */
    QcStandardDocument getLinkedDocument(String standardId);

    /**
     * Create or update linked source document metadata from structured standard.
     */
    QcStandardDocument syncLinkedDocument(QcQualityStandard standard);

    /**
     * Remove linked document clauses, vectors, metadata, and stored files.
     */
    void removeLinkedDocument(String standardId);

    /**
     * Purge indexed clauses and vectors for the linked document.
     */
    void purgeLinkedVectors(String standardId);

    /**
     * Ingest linked PDF for a published standard.
     */
    StandardDocumentIngestVO ingestLinkedDocument(String standardId);

    /**
     * Build source document summary for standard detail page.
     */
    StandardSourceDocumentVO buildSourceDocumentSummary(String standardId);
}
