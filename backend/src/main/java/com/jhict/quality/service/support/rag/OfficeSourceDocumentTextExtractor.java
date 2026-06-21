package com.jhict.quality.service.support.rag;

import org.apache.poi.extractor.ExtractorFactory;
import org.apache.poi.extractor.POITextExtractor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.file.Path;

@Component
public class OfficeSourceDocumentTextExtractor implements SourceDocumentTextExtractor {

    @Override
    public boolean supports(String fileType) {
        return "doc".equals(fileType) || "docx".equals(fileType)
                || "xls".equals(fileType) || "xlsx".equals(fileType)
                || "ppt".equals(fileType) || "pptx".equals(fileType);
    }

    @Override
    public ExtractedStandardDocument extract(String sourceFileName, String sourceFilePath,
                                             Path path, String fileType) throws Exception {
        ExtractedStandardDocument document = DocumentExtractionUtils.baseDocument(sourceFileName, sourceFilePath, fileType);
        POITextExtractor extractor = ExtractorFactory.createExtractor(path.toFile());
        try {
            String text = DocumentExtractionUtils.cleanText(extractor.getText());
            if (StringUtils.hasText(text)) {
                document.getSegments().add(new DocumentTextSegment(null, text));
            }
        } finally {
            extractor.close();
        }
        return document;
    }
}
