package com.jhict.quality.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jhict.quality.common.exception.ServiceException;
import com.jhict.quality.dto.StandardClausePageQuery;
import com.jhict.quality.entity.QcQualityStandard;
import com.jhict.quality.entity.QcStandardDocument;
import com.jhict.quality.mapper.QcQualityStandardMapper;
import com.jhict.quality.mapper.QcStandardDocumentMapper;
import com.jhict.quality.service.api.StandardDocumentService;
import com.jhict.quality.vo.StandardClauseVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StandardSourceFileServiceImplTest {

    private static final String STANDARD_ID = "std-1";
    private static final String DOCUMENT_ID = "doc-1";
    private static final String OTHER_DOCUMENT_ID = "doc-2";
    private static final String CLAUSE_ID = "clause-1";

    @Mock
    private QcQualityStandardMapper qualityStandardMapper;

    @Mock
    private QcStandardDocumentMapper standardDocumentMapper;

    @Mock
    private StandardDocumentService standardDocumentService;

    private StandardSourceFileServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new StandardSourceFileServiceImpl();
        ReflectionTestUtils.setField(service, "qualityStandardMapper", qualityStandardMapper);
        ReflectionTestUtils.setField(service, "standardDocumentMapper", standardDocumentMapper);
        ReflectionTestUtils.setField(service, "standardDocumentService", standardDocumentService);
    }

    @Test
    void pageSourceFileClauses_shouldRejectWhenDocumentNotLinked() {
        when(qualityStandardMapper.selectById(STANDARD_ID)).thenReturn(standard());
        when(standardDocumentService.getLinkedDocument(STANDARD_ID, DOCUMENT_ID)).thenReturn(null);

        assertThrows(ServiceException.class,
                () -> service.pageSourceFileClauses(STANDARD_ID, DOCUMENT_ID, new StandardClausePageQuery()));
    }

    @Test
    void pageSourceFileClauses_shouldForceDocumentIdFromPath() {
        when(qualityStandardMapper.selectById(STANDARD_ID)).thenReturn(standard());
        when(standardDocumentService.getLinkedDocument(STANDARD_ID, DOCUMENT_ID)).thenReturn(linkedDocument());
        Page<StandardClauseVO> page = new Page<>(1, 20, 1);
        when(standardDocumentService.pageClauses(any(StandardClausePageQuery.class))).thenReturn(page);

        StandardClausePageQuery query = new StandardClausePageQuery();
        query.setDocumentId(OTHER_DOCUMENT_ID);
        query.setStandardId("other-standard");
        query.setKeyword("Rm");
        query.setPageNum(2);
        query.setPageSize(10);

        service.pageSourceFileClauses(STANDARD_ID, DOCUMENT_ID, query);

        ArgumentCaptor<StandardClausePageQuery> captor = ArgumentCaptor.forClass(StandardClausePageQuery.class);
        verify(standardDocumentService).pageClauses(captor.capture());
        StandardClausePageQuery actual = captor.getValue();
        assertEquals(DOCUMENT_ID, actual.getDocumentId());
        assertEquals(null, actual.getStandardId());
        assertEquals("Rm", actual.getKeyword());
        assertEquals(Integer.valueOf(2), actual.getPageNum());
        assertEquals(Integer.valueOf(10), actual.getPageSize());
    }

    @Test
    void getSourceFileClause_shouldRejectWhenClauseBelongsToOtherDocument() {
        when(qualityStandardMapper.selectById(STANDARD_ID)).thenReturn(standard());
        when(standardDocumentService.getLinkedDocument(STANDARD_ID, DOCUMENT_ID)).thenReturn(linkedDocument());
        StandardClauseVO clause = new StandardClauseVO();
        clause.setId(CLAUSE_ID);
        clause.setDocumentId(OTHER_DOCUMENT_ID);
        when(standardDocumentService.getClauseById(CLAUSE_ID)).thenReturn(clause);

        assertThrows(ServiceException.class,
                () -> service.getSourceFileClause(STANDARD_ID, DOCUMENT_ID, CLAUSE_ID));
    }

    @Test
    void getSourceFileClause_shouldReturnClauseWhenDocumentMatches() {
        when(qualityStandardMapper.selectById(STANDARD_ID)).thenReturn(standard());
        when(standardDocumentService.getLinkedDocument(STANDARD_ID, DOCUMENT_ID)).thenReturn(linkedDocument());
        StandardClauseVO clause = new StandardClauseVO();
        clause.setId(CLAUSE_ID);
        clause.setDocumentId(DOCUMENT_ID);
        clause.setClauseNo("5.1");
        when(standardDocumentService.getClauseById(CLAUSE_ID)).thenReturn(clause);

        StandardClauseVO result = service.getSourceFileClause(STANDARD_ID, DOCUMENT_ID, CLAUSE_ID);

        assertEquals(CLAUSE_ID, result.getId());
        assertEquals("5.1", result.getClauseNo());
        verify(standardDocumentService).getClauseById(eq(CLAUSE_ID));
    }

    private QcQualityStandard standard() {
        QcQualityStandard standard = new QcQualityStandard();
        standard.setId(STANDARD_ID);
        standard.setStatus("PUBLISHED");
        return standard;
    }

    private QcStandardDocument linkedDocument() {
        QcStandardDocument document = new QcStandardDocument();
        document.setId(DOCUMENT_ID);
        document.setStandardId(STANDARD_ID);
        return document;
    }
}
