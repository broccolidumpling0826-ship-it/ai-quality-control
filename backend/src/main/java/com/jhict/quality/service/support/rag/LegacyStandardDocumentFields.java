package com.jhict.quality.service.support.rag;

import com.jhict.quality.entity.QcStandardDocument;
import org.springframework.util.StringUtils;

import java.util.Locale;

/**
 * Backward compatibility for legacy qc_standard_document file_* columns.
 */
public final class LegacyStandardDocumentFields {

    private static final String DEFAULT_FILE_TYPE = "pdf";
    private static final String PENDING_PATH = "pending";

    private LegacyStandardDocumentFields() {
    }

    public static void applyPendingFileFields(QcStandardDocument document, String standardCode) {
        String pendingName = StringUtils.hasText(standardCode) ? standardCode + ".pdf" : "pending.pdf";
        document.setFileName(pendingName);
        document.setFilePath(PENDING_PATH);
        document.setFileType(DEFAULT_FILE_TYPE);
        document.setFileSize(0L);
    }

    public static void applyUploadedFileFields(QcStandardDocument document, String fileName,
                                               String filePath, long fileSize) {
        applyUploadedFileFields(document, fileName, filePath, fileSize, extensionOf(fileName));
    }

    public static void applyUploadedFileFields(QcStandardDocument document, String fileName,
                                               String filePath, long fileSize, String fileType) {
        document.setSourceFileName(fileName);
        document.setSourceFilePath(filePath);
        document.setFileName(fileName);
        document.setFilePath(filePath);
        document.setFileType(StringUtils.hasText(fileType) ? fileType : DEFAULT_FILE_TYPE);
        document.setFileSize(fileSize);
    }

    private static String extensionOf(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            return DEFAULT_FILE_TYPE;
        }
        int index = fileName.lastIndexOf('.');
        if (index < 0 || index == fileName.length() - 1) {
            return DEFAULT_FILE_TYPE;
        }
        return fileName.substring(index + 1).toLowerCase(Locale.ROOT);
    }
}
