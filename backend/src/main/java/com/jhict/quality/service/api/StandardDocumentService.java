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

import java.util.Collection;
import java.util.List;

public interface StandardDocumentService {

    IPage<StandardDocumentVO> pageDocuments(StandardDocumentPageQuery query);

    StandardDocumentVO getDocumentById(String id);

    IPage<StandardClauseVO> pageClauses(StandardClausePageQuery query);

    StandardClauseVO getClauseById(String id);

    List<StandardClauseVO> listClausesByIds(List<String> clauseIds);

    VectorIndexResponse indexPreparedClauses(PreparedClauseIndexCmd cmd);

    StandardDocumentIngestVO ingestAndIndexDocument(StandardDocumentIngestCmd cmd);

    List<QcStandardDocument> listLinkedDocuments(String standardId);

    QcStandardDocument getLinkedDocument(String standardId, String documentId);

    QcStandardDocument createLinkedDocumentForUpload(QcQualityStandard standard);

    void syncLinkedDocumentsMetadata(QcQualityStandard standard);

    void removeLinkedDocument(String standardId);

    void removeLinkedDocumentFile(String standardId, String documentId);

    void purgeDocumentVectors(String documentId);

    StandardDocumentIngestVO ingestDocument(String documentId);

    List<StandardDocumentIngestVO> ingestAllLinkedDocuments(String standardId);

    StandardSourceDocumentVO buildSourceDocumentSummary(QcStandardDocument document);

    List<StandardSourceDocumentVO> listSourceDocumentSummaries(String standardId);

    StandardSourceDocumentVO buildSourceDocumentSummary(String standardId);

    int countLinkedSourceFiles(String standardId);

    List<QcStandardDocument> listDocumentsByIds(Collection<String> documentIds);

    /** @deprecated use {@link #listLinkedDocuments(String)} */
    default QcStandardDocument getLinkedDocument(String standardId) {
        List<QcStandardDocument> documents = listLinkedDocuments(standardId);
        return documents.isEmpty() ? null : documents.get(documents.size() - 1);
    }

    /** @deprecated use {@link #syncLinkedDocumentsMetadata(QcQualityStandard)} */
    default QcStandardDocument syncLinkedDocument(QcQualityStandard standard) {
        syncLinkedDocumentsMetadata(standard);
        return getLinkedDocument(standard.getId());
    }

    /** @deprecated use {@link #purgeDocumentVectors(String)} per document */
    default void purgeLinkedVectors(String standardId) {
        for (QcStandardDocument document : listLinkedDocuments(standardId)) {
            purgeDocumentVectors(document.getId());
        }
    }

    /** @deprecated use {@link #ingestAllLinkedDocuments(String)} */
    default StandardDocumentIngestVO ingestLinkedDocument(String standardId) {
        List<StandardDocumentIngestVO> results = ingestAllLinkedDocuments(standardId);
        return results.isEmpty() ? null : results.get(0);
    }
}
