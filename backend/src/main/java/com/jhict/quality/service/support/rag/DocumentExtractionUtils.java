package com.jhict.quality.service.support.rag;

final class DocumentExtractionUtils {

    private DocumentExtractionUtils() {
    }

    static ExtractedStandardDocument baseDocument(String sourceFileName, String sourceFilePath, String fileType) {
        ExtractedStandardDocument document = new ExtractedStandardDocument();
        document.setSourceFileName(sourceFileName);
        document.setSourceFilePath(sourceFilePath);
        document.setFileType(fileType);
        return document;
    }

    static String cleanText(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("\r\n", "\n")
                .replace('\r', '\n')
                .replace('\u00A0', ' ')
                .trim();
    }

    /**
     * Strip DeepSeek-OCR grounding markup such as {@code <|ref|>...<|/ref|>} and
     * {@code <|det|>[[x,y,w,h]]<|/det|>}, keeping only readable text for chunking/RAG.
     */
    static String cleanVisionOcrText(String text) {
        String cleaned = cleanText(text);
        if (!cleaned.contains("<|")) {
            return cleaned;
        }
        cleaned = cleaned.replaceAll("(?s)<\\|[^|]+\\|>.*?<\\|/[^|]+\\|>", "");
        cleaned = cleaned.replaceAll("<\\|[^|]*\\|>", "");
        cleaned = cleaned.replaceAll("(?m)^[ \\t]+", "");
        return cleaned.trim();
    }
}
