package com.jhict.quality.service.support.rag;

import com.jhict.quality.gateway.model.ModelGateway;
import com.jhict.quality.gateway.model.ModelVisionExtractionRequest;
import com.jhict.quality.gateway.model.ModelVisionExtractionResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ImageVisionSourceDocumentTextExtractorTest {

    @TempDir
    Path tempDir;

    @Test
    void extractShouldUseModelGatewayOcrResult() throws Exception {
        Path png = tempDir.resolve("scan.png");
        Files.write(png, new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00});

        ModelGateway modelGateway = mock(ModelGateway.class);
        when(modelGateway.extractImageText(any(ModelVisionExtractionRequest.class)))
                .thenReturn(ModelVisionExtractionResponse.builder()
                        .success(true)
                        .extractedText("7.3 力学性能\nQ235B 屈服强度 ReL 应不小于 235 MPa。")
                        .build());

        ImageVisionSourceDocumentTextExtractor extractor = new ImageVisionSourceDocumentTextExtractor(modelGateway);
        ExtractedStandardDocument document = extractor.extract(
                png.getFileName().toString(),
                png.toString(),
                png,
                "png");

        assertThat(document.getSegments()).hasSize(1);
        assertThat(document.getSegments().get(0).getText()).contains("屈服强度");
    }
}
