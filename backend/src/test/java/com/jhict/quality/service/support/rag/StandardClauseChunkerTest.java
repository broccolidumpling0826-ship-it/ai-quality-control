package com.jhict.quality.service.support.rag;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StandardClauseChunkerTest {

    private final StandardClauseChunker chunker = new StandardClauseChunker();

    @Test
    void chunkShouldPreferClauseHeadingAndParagraphBoundary() {
        ExtractedStandardDocument document = new ExtractedStandardDocument();
        document.setSegments(Arrays.asList(new DocumentTextSegment(3,
                "7.3 力学性能\n" +
                        "Q235B 屈服强度 ReL 应不小于 235 MPa。\n\n" +
                        "7.4 表面质量\n" +
                        "钢卷表面不得有影响使用的裂纹、结疤。")));

        List<StandardClauseChunk> chunks = chunker.chunk(document, 1200);

        assertThat(chunks).hasSize(2);
        assertThat(chunks.get(0).getClauseNo()).isEqualTo("7.3");
        assertThat(chunks.get(0).getPageNo()).isEqualTo(3);
        assertThat(chunks.get(0).getParagraphText()).contains("屈服强度");
        assertThat(chunks.get(1).getClauseNo()).isEqualTo("7.4");
    }

    @Test
    void chunkShouldSplitOcrParagraphByClauseNumber() {
        ExtractedStandardDocument document = new ExtractedStandardDocument();
        document.setSegments(Arrays.asList(new DocumentTextSegment(1,
                "Q345B低合金高强度结构钢企业标准 标准编号：Q/ZX-MULTI-1782065642 "
                        + "5.1 抗拉强度 Rm Q345B 热轧板抗拉强度 Rm 的合格范围为 470 MPa 至 630 MPa。"
                        + "5.2 屈服强度 ReL Q345B 热轧板下屈服强度 ReL 的合格下限为 345 MPa。"
                        + "6.1 厚度公差 Q345B 热轧板厚度公差应控制在 -0.200 mm 至 0.200 mm。")));

        List<StandardClauseChunk> chunks = chunker.chunk(document, 1200);

        assertThat(chunks).hasSizeGreaterThanOrEqualTo(3);
        assertThat(chunks.stream().map(StandardClauseChunk::getClauseNo))
                .anyMatch(no -> "5.1".equals(no) || no.startsWith("5.1"));
        assertThat(chunks.stream().map(StandardClauseChunk::getClauseNo))
                .anyMatch(no -> "5.2".equals(no) || no.startsWith("5.2"));
        assertThat(chunks.stream().map(StandardClauseChunk::getClauseNo))
                .anyMatch(no -> "6.1".equals(no) || no.startsWith("6.1"));
    }

    @Test
    void chunkShouldSplitOversizedClauseBySentenceBoundary() {
        StringBuilder paragraph = new StringBuilder("8.1 让步接收\n");
        for (int i = 0; i < 20; i++) {
            paragraph.append("偏差应评估客户用途、历史投诉和替代资源。");
        }
        ExtractedStandardDocument document = new ExtractedStandardDocument();
        document.setSegments(Arrays.asList(new DocumentTextSegment(1, paragraph.toString())));

        List<StandardClauseChunk> chunks = chunker.chunk(document, 200);

        assertThat(chunks).hasSizeGreaterThan(1);
        assertThat(chunks.get(0).getClauseNo()).startsWith("8.1-");
        assertThat(chunks).allMatch(chunk -> chunk.getParagraphText().length() <= 200);
    }
}
