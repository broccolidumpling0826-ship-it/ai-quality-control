package com.jhict.quality.ai.rag;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jhict.quality.entity.QcQualityStandard;
import com.jhict.quality.entity.QcStandardDocumentChunk;
import com.jhict.quality.mapper.QcQualityStandardMapper;
import com.jhict.quality.mapper.QcStandardDocumentChunkMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class RagRetriever {

    private static final int RECALL_LIMIT = 50;
    private static final double BM25_K1 = 1.2;
    private static final double BM25_B = 0.75;

    @Resource
    private QcStandardDocumentChunkMapper chunkMapper;

    @Resource
    private QcQualityStandardMapper qualityStandardMapper;

    public List<QcStandardDocumentChunk> retrieve(String query, String standardId, String standardType, int topK) {
        if (!StringUtils.hasText(query)) {
            return Collections.emptyList();
        }
        String resolvedStandardId = resolveStandardId(standardId, standardType);
        List<QcStandardDocumentChunk> candidates = chunkMapper.fulltextSearch(query, resolvedStandardId, RECALL_LIMIT);
        if (candidates.isEmpty()) {
            candidates = chunkMapper.keywordFallbackSearch(query, resolvedStandardId, RECALL_LIMIT);
        }
        if (candidates.isEmpty()) {
            return Collections.emptyList();
        }
        return bm25Rerank(query, candidates, topK);
    }

    private String resolveStandardId(String standardId, String standardType) {
        if (StringUtils.hasText(standardId)) {
            return standardId;
        }
        if (!StringUtils.hasText(standardType)) {
            return null;
        }
        QcQualityStandard std = qualityStandardMapper.selectOne(
                new LambdaQueryWrapper<QcQualityStandard>()
                        .eq(QcQualityStandard::getStandardType, standardType)
                        .eq(QcQualityStandard::getStatus, "PUBLISHED")
                        .last("LIMIT 1"));
        return std != null ? std.getId() : null;
    }

    private List<QcStandardDocumentChunk> bm25Rerank(String query, List<QcStandardDocumentChunk> candidates, int topK) {
        List<String> queryTerms = tokenize(query);
        if (queryTerms.isEmpty()) {
            return candidates.stream().limit(topK).collect(Collectors.toList());
        }

        double avgDocLen = candidates.stream()
                .mapToInt(c -> tokenize(c.getChunkText() + " " + nullToEmpty(c.getKeywordTags())).size())
                .average().orElse(1.0);

        Map<String, Integer> docFreq = new HashMap<>();
        for (QcStandardDocumentChunk chunk : candidates) {
            Set<String> terms = new HashSet<>(tokenize(chunk.getChunkText() + " " + nullToEmpty(chunk.getKeywordTags())));
            for (String term : terms) {
                docFreq.merge(term, 1, Integer::sum);
            }
        }
        int n = candidates.size();

        List<ScoredChunk> scored = new ArrayList<>();
        for (QcStandardDocumentChunk chunk : candidates) {
            List<String> docTerms = tokenize(chunk.getChunkText() + " " + nullToEmpty(chunk.getKeywordTags()));
            double score = bm25Score(queryTerms, docTerms, docFreq, n, avgDocLen);
            scored.add(new ScoredChunk(chunk, score));
        }
        scored.sort((a, b) -> Double.compare(b.score, a.score));
        return scored.stream().limit(topK).map(s -> s.chunk).collect(Collectors.toList());
    }

    private double bm25Score(List<String> queryTerms, List<String> docTerms,
                             Map<String, Integer> docFreq, int n, double avgDocLen) {
        Map<String, Long> termFreq = docTerms.stream()
                .collect(Collectors.groupingBy(t -> t, Collectors.counting()));
        double docLen = docTerms.size();
        double score = 0;
        for (String term : queryTerms) {
            long tf = termFreq.getOrDefault(term, 0L);
            if (tf == 0) {
                continue;
            }
            int df = docFreq.getOrDefault(term, 0);
            double idf = Math.log(1 + (n - df + 0.5) / (df + 0.5));
            double numerator = tf * (BM25_K1 + 1);
            double denominator = tf + BM25_K1 * (1 - BM25_B + BM25_B * docLen / avgDocLen);
            score += idf * numerator / denominator;
        }
        return score;
    }

    private List<String> tokenize(String text) {
        if (!StringUtils.hasText(text)) {
            return Collections.emptyList();
        }
        return Arrays.stream(text.toLowerCase(Locale.ROOT).split("[\\s,，。；;、]+"))
                .filter(StringUtils::hasText)
                .collect(Collectors.toList());
    }

    private String nullToEmpty(String value) {
        return value != null ? value : "";
    }

    private static class ScoredChunk {
        private final QcStandardDocumentChunk chunk;
        private final double score;

        private ScoredChunk(QcStandardDocumentChunk chunk, double score) {
            this.chunk = chunk;
            this.score = score;
        }
    }
}
