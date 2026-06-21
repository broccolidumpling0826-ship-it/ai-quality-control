package com.jhict.quality.service.impl;

import com.jhict.quality.common.entity.ApiResult;
import com.jhict.quality.common.exception.ServiceException;
import com.jhict.quality.entity.QcQualityStandard;
import com.jhict.quality.entity.QcStandardDocument;
import com.jhict.quality.mapper.QcQualityStandardMapper;
import com.jhict.quality.mapper.QcStandardDocumentMapper;
import com.jhict.quality.service.api.StandardDocumentService;
import com.jhict.quality.service.api.StandardSourceFileService;
import com.jhict.quality.service.support.rag.LegacyStandardDocumentFields;
import com.jhict.quality.service.support.rag.StandardSourceFileStorageService;
import com.jhict.quality.service.support.rag.StandardSourceFileStorageService.StoredStandardFile;
import com.jhict.quality.vo.StandardDocumentIngestVO;
import com.jhict.quality.vo.StandardSourceDocumentVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@Service
public class StandardSourceFileServiceImpl implements StandardSourceFileService {

    @Resource
    private QcQualityStandardMapper qualityStandardMapper;

    @Resource
    private QcStandardDocumentMapper standardDocumentMapper;

    @Resource
    private StandardDocumentService standardDocumentService;

    @Resource
    private StandardSourceFileStorageService standardSourceFileStorageService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StandardSourceDocumentVO uploadSourceFile(String standardId, MultipartFile file) {
        QcQualityStandard standard = requireStandard(standardId);
        QcStandardDocument document = standardDocumentService.syncLinkedDocument(standard);
        boolean published = "PUBLISHED".equals(standard.getStatus());
        if (published) {
            standardDocumentService.purgeLinkedVectors(standardId);
        }

        StoredStandardFile stored = standardSourceFileStorageService.save(standardId, file);
        updateSourceFileMetadata(document.getId(), stored);

        if (published) {
            try {
                standardDocumentService.ingestLinkedDocument(standardId);
            } catch (Exception ex) {
                log.warn("已发布标准重传后立即索引失败，standardId={}, error={}", standardId, ex.getMessage());
            }
        }
        return standardDocumentService.buildSourceDocumentSummary(standardId);
    }

    @Override
    public void downloadSourceFile(String standardId, HttpServletResponse response) {
        requireStandard(standardId);
        QcStandardDocument document = standardDocumentService.getLinkedDocument(standardId);
        if (document == null || !StringUtils.hasText(document.getSourceFilePath())) {
            throw new ServiceException(ApiResult.CODE_NOT_FOUND, "标准源 PDF 不存在");
        }
        Path path = standardSourceFileStorageService.resolveReadablePath(document.getSourceFilePath());
        String fileName = StringUtils.hasText(document.getSourceFileName())
                ? document.getSourceFileName() : "standard-source.pdf";
        try {
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition",
                    "attachment; filename=\"" + URLEncoder.encode(fileName, StandardCharsets.UTF_8.name()) + "\"");
            response.setContentLengthLong(Files.size(path));
            try (OutputStream outputStream = response.getOutputStream()) {
                Files.copy(path, outputStream);
                outputStream.flush();
            }
        } catch (IOException ex) {
            throw new ServiceException(ApiResult.CODE_SERVER_ERROR, "下载标准源 PDF 失败: " + ex.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StandardDocumentIngestVO reindexSourceFile(String standardId) {
        QcQualityStandard standard = requireStandard(standardId);
        if (!"PUBLISHED".equals(standard.getStatus())) {
            throw new ServiceException(ApiResult.CODE_BAD_REQUEST, "仅已发布标准可重新索引源 PDF");
        }
        QcStandardDocument document = standardDocumentService.getLinkedDocument(standardId);
        if (document == null || !StringUtils.hasText(document.getSourceFilePath())) {
            throw new ServiceException(ApiResult.CODE_BAD_REQUEST, "标准未上传源 PDF，无法重新索引");
        }
        standardDocumentService.purgeLinkedVectors(standardId);
        return standardDocumentService.ingestLinkedDocument(standardId);
    }

    private QcQualityStandard requireStandard(String standardId) {
        if (!StringUtils.hasText(standardId)) {
            throw new ServiceException(ApiResult.CODE_BAD_REQUEST, "标准ID不能为空");
        }
        QcQualityStandard standard = qualityStandardMapper.selectById(standardId);
        if (standard == null) {
            throw new ServiceException(ApiResult.CODE_NOT_FOUND, "质量标准不存在");
        }
        return standard;
    }

    private void updateSourceFileMetadata(String documentId, StoredStandardFile stored) {
        QcStandardDocument update = new QcStandardDocument();
        update.setId(documentId);
        LegacyStandardDocumentFields.applyUploadedFileFields(
                update,
                stored.getOriginalFileName(),
                stored.getRelativePath(),
                stored.getAbsolutePath().toFile().length());
        update.setSourceFileHash(stored.getFileHash());
        update.setParseStatus("PENDING");
        update.setIndexStatus("PENDING");
        update.setParseErrorMessage(null);
        update.setIndexedAt(null);
        standardDocumentMapper.updateById(update);
    }
}
