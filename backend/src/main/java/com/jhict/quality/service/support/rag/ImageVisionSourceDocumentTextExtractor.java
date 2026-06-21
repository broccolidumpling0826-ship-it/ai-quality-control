package com.jhict.quality.service.support.rag;

import com.jhict.quality.common.entity.ApiResult;
import com.jhict.quality.common.exception.ServiceException;
import com.jhict.quality.gateway.model.ModelGateway;
import com.jhict.quality.gateway.model.ModelVisionExtractionRequest;
import com.jhict.quality.gateway.model.ModelVisionExtractionResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.UUID;

@Component
public class ImageVisionSourceDocumentTextExtractor implements SourceDocumentTextExtractor {

    private static final String DEFAULT_SYSTEM_PROMPT =
            "You are an OCR engine for quality standard documents. "
                    + "Extract all visible text faithfully. Do not summarize, invent, or add content not present in the image.";

    private final ModelGateway modelGateway;

    public ImageVisionSourceDocumentTextExtractor(ModelGateway modelGateway) {
        this.modelGateway = modelGateway;
    }

    @Override
    public boolean supports(String fileType) {
        return "png".equals(fileType) || "jpg".equals(fileType) || "jpeg".equals(fileType);
    }

    @Override
    public ExtractedStandardDocument extract(String sourceFileName, String sourceFilePath,
                                             Path path, String fileType) throws Exception {
        byte[] bytes = Files.readAllBytes(path);
        if (bytes.length == 0) {
            throw new ServiceException(ApiResult.CODE_BAD_REQUEST, "图片源文件为空");
        }
        String mimeType = mimeType(fileType);
        String base64 = Base64.getEncoder().encodeToString(bytes);

        ModelVisionExtractionResponse response = modelGateway.extractImageText(ModelVisionExtractionRequest.builder()
                .traceId(UUID.randomUUID().toString())
                .businessType("STANDARD_SOURCE_OCR")
                .businessId(sourceFilePath)
                .imageBase64(base64)
                .imageMimeType(mimeType)
                .systemPrompt(DEFAULT_SYSTEM_PROMPT)
                .build());

        if (response == null || !response.isSuccess() || !StringUtils.hasText(response.getExtractedText())) {
            String message = response != null && StringUtils.hasText(response.getErrorMessage())
                    ? response.getErrorMessage()
                    : "Vision OCR 未返回有效文本";
            throw new ServiceException(ApiResult.CODE_SERVER_ERROR, message);
        }

        ExtractedStandardDocument document = DocumentExtractionUtils.baseDocument(sourceFileName, sourceFilePath, fileType);
        document.getSegments().add(new DocumentTextSegment(1, DocumentExtractionUtils.cleanText(response.getExtractedText())));
        return document;
    }

    private String mimeType(String fileType) {
        if ("png".equals(fileType)) {
            return "image/png";
        }
        return "image/jpeg";
    }
}
