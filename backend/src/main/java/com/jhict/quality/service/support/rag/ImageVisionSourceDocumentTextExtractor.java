package com.jhict.quality.service.support.rag;

import com.jhict.quality.gateway.model.ModelGateway;
import com.jhict.quality.gateway.model.ModelVisionExtractionRequest;
import com.jhict.quality.gateway.model.ModelVisionExtractionResponse;
import com.jhict.quality.service.support.prompt.VisionOcrPromptProvider;
import com.jhict.quality.common.entity.ApiResult;
import com.jhict.quality.common.exception.ServiceException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.UUID;

@Component
@Slf4j
public class ImageVisionSourceDocumentTextExtractor implements SourceDocumentTextExtractor {

    private final ModelGateway modelGateway;
    private final VisionOcrPromptProvider visionOcrPromptProvider;

    public ImageVisionSourceDocumentTextExtractor(ModelGateway modelGateway,
                                                  VisionOcrPromptProvider visionOcrPromptProvider) {
        this.modelGateway = modelGateway;
        this.visionOcrPromptProvider = visionOcrPromptProvider;
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
                .systemPrompt(visionOcrPromptProvider.resolveSystemPrompt())
                .ocrPrompt(visionOcrPromptProvider.resolveOcrPrompt())
                .build());

        if (response == null || !response.isSuccess() || !StringUtils.hasText(response.getExtractedText())) {
            String message = response != null && StringUtils.hasText(response.getErrorMessage())
                    ? response.getErrorMessage()
                    : "Vision OCR 未返回有效文本";
            if (response != null && StringUtils.hasText(response.getRawResponse())) {
                log.warn("Vision OCR 无有效文本，businessId={}, model={}, finishReason={}, raw={}",
                        sourceFilePath,
                        response.getModelName(),
                        response.getFinishReason(),
                        response.getRawResponse().length() > 500
                                ? response.getRawResponse().substring(0, 500) + "..."
                                : response.getRawResponse());
            }
            throw new ServiceException(ApiResult.CODE_SERVER_ERROR, message);
        }

        String cleanedText = DocumentExtractionUtils.cleanVisionOcrText(response.getExtractedText());
        if (!StringUtils.hasText(cleanedText)) {
            throw new ServiceException(ApiResult.CODE_SERVER_ERROR,
                    "Vision OCR 返回内容经清洗后为空，请检查 OCR 模型输出或图片质量");
        }

        ExtractedStandardDocument document = DocumentExtractionUtils.baseDocument(sourceFileName, sourceFilePath, fileType);
        document.getSegments().add(new DocumentTextSegment(1, cleanedText));
        return document;
    }

    private String mimeType(String fileType) {
        if ("png".equals(fileType)) {
            return "image/png";
        }
        return "image/jpeg";
    }
}
