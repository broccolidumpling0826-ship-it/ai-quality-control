package com.jhict.quality.service.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jhict.quality.dto.StandardClausePageQuery;
import com.jhict.quality.vo.StandardClauseVO;
import com.jhict.quality.vo.StandardDocumentIngestVO;
import com.jhict.quality.vo.StandardSourceDocumentVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

public interface StandardSourceFileService {

    List<StandardSourceDocumentVO> listSourceFiles(String standardId);

    StandardSourceDocumentVO uploadSourceFile(String standardId, MultipartFile file);

    void downloadSourceFile(String standardId, String documentId, HttpServletResponse response);

    void deleteSourceFile(String standardId, String documentId);

    StandardDocumentIngestVO reindexSourceFile(String standardId, String documentId);

    List<StandardDocumentIngestVO> reindexAllSourceFiles(String standardId);

    IPage<StandardClauseVO> pageSourceFileClauses(String standardId, String documentId, StandardClausePageQuery query);

    StandardClauseVO getSourceFileClause(String standardId, String documentId, String clauseId);

    default void downloadSourceFile(String standardId, HttpServletResponse response) {
        List<StandardSourceDocumentVO> files = listSourceFiles(standardId);
        StandardSourceDocumentVO latest = null;
        for (StandardSourceDocumentVO file : files) {
            if (Boolean.TRUE.equals(file.getHasSourceFile())) {
                latest = file;
            }
        }
        if (latest == null) {
            throw new com.jhict.quality.common.exception.ServiceException(
                    com.jhict.quality.common.entity.ApiResult.CODE_NOT_FOUND, "标准源文件不存在");
        }
        downloadSourceFile(standardId, latest.getDocumentId(), response);
    }

    default StandardDocumentIngestVO reindexSourceFile(String standardId) {
        List<StandardDocumentIngestVO> results = reindexAllSourceFiles(standardId);
        return results.isEmpty() ? null : results.get(0);
    }
}
