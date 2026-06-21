package com.jhict.quality.service.support.rag;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.file.Path;

@Component
public class PdfSourceDocumentTextExtractor implements SourceDocumentTextExtractor {

    @Override
    public boolean supports(String fileType) {
        return "pdf".equals(fileType);
    }

    @Override
    public ExtractedStandardDocument extract(String sourceFileName, String sourceFilePath,
                                             Path path, String fileType) throws Exception {
        ExtractedStandardDocument document = DocumentExtractionUtils.baseDocument(sourceFileName, sourceFilePath, fileType);
        try (PDDocument pdf = PDDocument.load(path.toFile())) {
            PDFTextStripper stripper = new PDFTextStripper();
            int pages = pdf.getNumberOfPages();
            for (int pageNo = 1; pageNo <= pages; pageNo++) {
                stripper.setStartPage(pageNo);
                stripper.setEndPage(pageNo);
                String text = DocumentExtractionUtils.cleanText(stripper.getText(pdf));
                if (StringUtils.hasText(text)) {
                    document.getSegments().add(new DocumentTextSegment(pageNo, text));
                }
            }
        }
        return document;
    }
}
