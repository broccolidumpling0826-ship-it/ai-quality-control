package com.jhict.quality.service.support.rag;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class PlainTextSourceDocumentTextExtractor implements SourceDocumentTextExtractor {

    @Override
    public boolean supports(String fileType) {
        return "txt".equals(fileType) || "md".equals(fileType)
                || "markdown".equals(fileType) || "csv".equals(fileType);
    }

    @Override
    public ExtractedStandardDocument extract(String sourceFileName, String sourceFilePath,
                                             Path path, String fileType) throws Exception {
        ExtractedStandardDocument document = DocumentExtractionUtils.baseDocument(sourceFileName, sourceFilePath, fileType);
        String text = DocumentExtractionUtils.cleanText(new String(Files.readAllBytes(path), StandardCharsets.UTF_8));
        if (StringUtils.hasText(text)) {
            document.getSegments().add(new DocumentTextSegment(null, text));
        }
        return document;
    }
}
