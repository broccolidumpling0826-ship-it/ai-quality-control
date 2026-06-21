package com.jhict.quality.service.support.rag;

import com.jhict.quality.entity.QcStandardDocument;
import org.springframework.util.StringUtils;

/**
 * Backward compatibility for legacy qc_standard_document file_* columns.
 */
public final class LegacyStandardDocumentFields {

    private static final String LEGACY_FILE_TYPE = "pdf";
    private static final String PENDING_PATH = "pending";

    private LegacyStandardDocumentFields() {
    }

    public static void applyPendingFileFields(QcStandardDocument document, String standardCode) {
        String pendingName = StringUtils.hasText(standardCode) ? standardCode + ".pdf" : "pending.pdf";
        document.setFileName(pendingName);
        document.setFilePath(PENDING_PATH);
        document.setFileType(LEGACY_FILE_TYPE);
        document.setFileSize(0L);
    }

    public static void applyUploadedFileFields(QcStandardDocument document, String fileName,
                                               String filePath, long fileSize) {
        document.setSourceFileName(fileName);
        document.setSourceFilePath(filePath);
        document.setFileName(fileName);
        document.setFilePath(filePath);
        document.setFileType(LEGACY_FILE_TYPE);
        document.setFileSize(fileSize);
    }
}
