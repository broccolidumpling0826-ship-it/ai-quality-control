package com.jhict.quality.service.api;

import com.jhict.quality.vo.StandardDocumentIngestVO;
import com.jhict.quality.vo.StandardSourceDocumentVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

/**
 * Orchestrates standard PDF upload, download, and reindex flows.
 */
public interface StandardSourceFileService {

    StandardSourceDocumentVO uploadSourceFile(String standardId, MultipartFile file);

    void downloadSourceFile(String standardId, HttpServletResponse response);

    StandardDocumentIngestVO reindexSourceFile(String standardId);
}
