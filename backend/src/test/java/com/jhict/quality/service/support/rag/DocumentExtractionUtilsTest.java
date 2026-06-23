package com.jhict.quality.service.support.rag;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentExtractionUtilsTest {

    @Test
    void cleanVisionOcrTextShouldStripGroundingTags() {
        String raw = "<|ref|>text<|/ref|><|det|>[[57, 329, 588, 349]]<|/det|> Q345B热轧板厚度公差应控制在 "
                + "\\(- 0.200\\mathrm{mm}\\) 至 \\(0.200\\mathrm{mm}\\) .\n"
                + "<|ref|>text<|/ref|><|det|>[[54, 360, 784, 380]]<|/det|> "
                + "注：本页为联调模拟扫描件，仅用于VisionOCR测试，不得用于生产判定。";

        String cleaned = DocumentExtractionUtils.cleanVisionOcrText(raw);

        assertThat(cleaned).doesNotContain("<|ref|").doesNotContain("<|det|").doesNotContain("[[57,");
        assertThat(cleaned).contains("Q345B热轧板厚度公差应控制在");
        assertThat(cleaned).contains("注：本页为联调模拟扫描件");
    }

    @Test
    void cleanVisionOcrTextShouldLeavePlainTextUnchanged() {
        assertThat(DocumentExtractionUtils.cleanVisionOcrText("7.3 力学性能\nReL 不小于 235 MPa。"))
                .isEqualTo("7.3 力学性能\nReL 不小于 235 MPa。");
    }
}
