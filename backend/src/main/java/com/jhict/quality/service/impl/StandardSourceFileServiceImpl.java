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
import java.util.ArrayList;
import java.util.List;

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
    public List<StandardSourceDocumentVO> listSourceFiles(String standardId) {
        requireStandard(standardId);
        return standardDocumentService.listSourceDocumentSummaries(standardId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StandardSourceDocumentVO uploadSourceFile(String standardId, MultipartFile file) {
        QcQualityStandard standard = requireStandard(standardId);
        standardSourceFileStorageService.assertCanAddFile(standardDocumentService.countLinkedSourceFiles(standardId));

        QcStandardDocument document = standardDocumentService.createLinkedDocumentForUpload(standard);
        StoredStandardFile stored = standardSourceFileStorageService.save(standardId, document.getId(), file);
        updateSourceFileMetadata(document.getId(), stored);

        if ("PUBLISHED".equals(standard.getStatus())) {
            try {
                standardDocumentService.ingestDocument(document.getId());
            } catch (Exception ex) {
                log.warn("已发布标准新增源文件后立即索引失败，standardId={}, documentId={}, error={}",
                        standardId, document.getId(), ex.getMessage());
            }
        }
        return standardDocumentService.buildSourceDocumentSummary(
                standardDocumentMapper.selectById(document.getId()));
    }

    @Override
    public void downloadSourceFile(String standardId, String documentId, HttpServletResponse response) {
        requireStandard(standardId);
        QcStandardDocument document = standardDocumentService.getLinkedDocument(standardId, documentId);
        if (document == null || !StringUtils.hasText(document.getSourceFilePath())) {
            throw new ServiceException(ApiResult.CODE_NOT_FOUND, "标准源文件不存在");
        }
        writeDownload(response, document);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSourceFile(String standardId, String documentId) {
        requireStandard(standardId);
        standardDocumentService.removeLinkedDocumentFile(standardId, documentId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StandardDocumentIngestVO reindexSourceFile(String standardId, String documentId) {
        QcQualityStandard standard = requireStandard(standardId);
        if (!"PUBLISHED".equals(standard.getStatus())) {
            throw new ServiceException(ApiResult.CODE_BAD_REQUEST, "仅已发布标准可重新索引源文件");
        }
        QcStandardDocument document = standardDocumentService.getLinkedDocument(standardId, documentId);
        if (document == null || !StringUtils.hasText(document.getSourceFilePath())) {
            throw new ServiceException(ApiResult.CODE_BAD_REQUEST, "标准源文件不存在，无法重新索引");
        }
        standardDocumentService.purgeDocumentVectors(documentId);
        return standardDocumentService.ingestDocument(documentId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<StandardDocumentIngestVO> reindexAllSourceFiles(String standardId) {
        QcQualityStandard standard = requireStandard(standardId);
        if (!"PUBLISHED".equals(standard.getStatus())) {
            throw new ServiceException(ApiResult.CODE_BAD_REQUEST, "仅已发布标准可重新索引源文件");
        }
        if (standardDocumentService.countLinkedSourceFiles(standardId) == 0) {
            throw new ServiceException(ApiResult.CODE_BAD_REQUEST, "标准未上传源文件，无法重新索引");
        }
        List<StandardDocumentIngestVO> results = new ArrayList<>();
        for (QcStandardDocument document : standardDocumentService.listLinkedDocuments(standardId)) {
            if (!StringUtils.hasText(document.getSourceFilePath())) {
                continue;
            }
            standardDocumentService.purgeDocumentVectors(document.getId());
            results.add(standardDocumentService.ingestDocument(document.getId()));
        }
        return results;
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
                stored.getAbsolutePath().toFile().length(),
                stored.getFileExtension());
        update.setSourceFileHash(stored.getFileHash());
        update.setParseStatus("PENDING");
        update.setIndexStatus("PENDING");
        update.setParseErrorMessage(null);
        update.setIndexedAt(null);
        standardDocumentMapper.updateById(update);
    }

    private void writeDownload(HttpServletResponse response, QcStandardDocument document) {
        Path path = standardSourceFileStorageService.resolveReadablePath(document.getSourceFilePath());
        String fileName = StringUtils.hasText(document.getSourceFileName())
                ? document.getSourceFileName() : "standard-source.bin";
        String extension = StringUtils.hasText(document.getFileType())
                ? document.getFileType() : fileExtension(fileName);
        try {
            response.setContentType(standardSourceFileStorageService.contentTypeForExtension(extension));
            response.setHeader("Content-Disposition",
                    "attachment; filename=\"" + URLEncoder.encode(fileName, StandardCharsets.UTF_8.name()) + "\"");
            response.setContentLengthLong(Files.size(path));
            try (OutputStream outputStream = response.getOutputStream()) {
                Files.copy(path, outputStream);
                outputStream.flush();
            }
        } catch (IOException ex) {
            throw new ServiceException(ApiResult.CODE_SERVER_ERROR, "下载标准源文件失败: " + ex.getMessage());
        }
    }

    private String fileExtension(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            return "pdf";
        }
        int index = fileName.lastIndexOf('.');
        if (index < 0 || index == fileName.length() - 1) {
            return "pdf";
        }
        return fileName.substring(index + 1).toLowerCase();
    }
}
