package com.jhict.quality.service.support.rag;

import com.jhict.quality.dto.StandardClausePageQuery;
import com.jhict.quality.service.api.StandardDocumentService;
import com.jhict.quality.vo.AiSourceReferenceVO;
import com.jhict.quality.vo.QcJudgmentResultVO;
import com.jhict.quality.vo.StandardClauseVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 判定解释、质保书问答、让步风险等场景共用的来源条款加载。
 */
@Slf4j
@Component
public class CitationReferenceLoader {

    @Resource
    private StandardDocumentService standardDocumentService;

    public List<AiSourceReferenceVO> loadForJudgment(List<String> standardIds,
                                                     List<QcJudgmentResultVO.EvidenceVO> evidences) {
        if (standardIds == null || standardIds.isEmpty()) {
            return Collections.emptyList();
        }
        Map<String, AiSourceReferenceVO> references = new LinkedHashMap<>();
        List<QcJudgmentResultVO.EvidenceVO> safeEvidences = evidences == null
                ? Collections.emptyList() : evidences;
        List<String> indicatorCodes = safeEvidences.stream()
                .map(QcJudgmentResultVO.EvidenceVO::getIndicatorCode)
                .filter(StringUtils::hasText)
                .distinct()
                .collect(Collectors.toList());
        for (String standardId : standardIds) {
            if (indicatorCodes.isEmpty()) {
                queryClausesByIndicator(references, standardId, null, 5);
            } else {
                for (String indicatorCode : indicatorCodes) {
                    queryClausesByIndicator(references, standardId, indicatorCode, 3);
                }
                for (QcJudgmentResultVO.EvidenceVO evidence : safeEvidences) {
                    for (String keyword : CitationReferenceSupport.buildIndicatorKeywords(evidence)) {
                        queryClausesByKeyword(references, standardId, keyword, 2);
                    }
                }
                if (references.isEmpty()) {
                    queryClausesByIndicator(references, standardId, null, 5);
                }
            }
            if (references.size() >= 12) {
                break;
            }
        }
        return CitationReferenceSupport.rankAndLimit(
                new ArrayList<>(references.values()), safeEvidences, 8);
    }

    public void queryAndMerge(Map<String, AiSourceReferenceVO> references, StandardClausePageQuery query) {
        if (references == null || query == null) {
            return;
        }
        try {
            for (StandardClauseVO clause : standardDocumentService.pageClauses(query).getRecords()) {
                references.putIfAbsent(clause.getId(), CitationReferenceSupport.fromClause(clause));
            }
        } catch (Exception e) {
            log.warn("查询来源条款失败，sourceType={}, standardId={}, keyword={}, error={}",
                    query.getSourceType(), query.getStandardId(), query.getKeyword(), e.getMessage());
        }
    }

    public void mergeByClauseId(Map<String, AiSourceReferenceVO> references,
                                Collection<AiSourceReferenceVO> toAdd) {
        CitationReferenceSupport.mergeByClauseId(references, toAdd);
    }

    private void queryClausesByKeyword(Map<String, AiSourceReferenceVO> references,
                                       String standardId,
                                       String keyword,
                                       int pageSize) {
        if (!StringUtils.hasText(keyword)) {
            return;
        }
        StandardClausePageQuery query = new StandardClausePageQuery();
        query.setStandardId(standardId);
        query.setKeyword(keyword);
        query.setPageNum(1);
        query.setPageSize(pageSize);
        queryAndMerge(references, query);
    }

    private void queryClausesByIndicator(Map<String, AiSourceReferenceVO> references,
                                         String standardId,
                                         String indicatorCode,
                                         int pageSize) {
        StandardClausePageQuery query = new StandardClausePageQuery();
        query.setStandardId(standardId);
        query.setIndicatorCode(indicatorCode);
        query.setPageNum(1);
        query.setPageSize(pageSize);
        queryAndMerge(references, query);
    }
}
