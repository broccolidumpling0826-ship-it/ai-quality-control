package com.jhict.quality.service.support.rag;

import com.jhict.quality.common.entity.ApiResult;
import com.jhict.quality.common.exception.ServiceException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;

@Component
public class StandardDocumentTextExtractor {

    private final List<SourceDocumentTextExtractor> extractors;

    public StandardDocumentTextExtractor(List<SourceDocumentTextExtractor> extractors) {
        this.extractors = extractors;
    }

    public ExtractedStandardDocument extract(String sourceFileName, String sourceFilePath) {
        if (!StringUtils.hasText(sourceFilePath)) {
            throw new ServiceException(ApiResult.CODE_BAD_REQUEST, "源文件路径不能为空");
        }
        if (sourceFilePath.startsWith("http://") || sourceFilePath.startsWith("https://")) {
            throw new ServiceException(ApiResult.CODE_BAD_REQUEST, "暂不支持从远程URL解析标准文档，请先上传到本地文件存储");
        }
        Path path = resolvePath(sourceFilePath);
        if (!Files.exists(path) || !Files.isRegularFile(path)) {
            throw new ServiceException(ApiResult.CODE_NOT_FOUND, "源文件不存在: " + sourceFilePath);
        }

        String fileType = fileType(sourceFileName, sourceFilePath);
        try {
            for (SourceDocumentTextExtractor extractor : extractors) {
                if (extractor.supports(fileType)) {
                    return extractor.extract(sourceFileName, sourceFilePath, path, fileType);
                }
            }
            throw new ServiceException(ApiResult.CODE_BAD_REQUEST, "不支持的标准文档类型: " + fileType);
        } catch (ServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ServiceException(ApiResult.CODE_SERVER_ERROR, "标准文档解析失败: " + ex.getMessage());
        }
    }

    private String fileType(String sourceFileName, String sourceFilePath) {
        String name = StringUtils.hasText(sourceFileName) ? sourceFileName : sourceFilePath;
        int index = name.lastIndexOf('.');
        if (index < 0 || index == name.length() - 1) {
            return "txt";
        }
        return name.substring(index + 1).toLowerCase(Locale.ROOT);
    }

    private Path resolvePath(String sourceFilePath) {
        Path path = Paths.get(sourceFilePath).normalize();
        if (Files.exists(path)) {
            return path;
        }
        if (!path.isAbsolute()) {
            Path projectRootPath = Paths.get("..").resolve(sourceFilePath).normalize();
            if (Files.exists(projectRootPath)) {
                return projectRootPath;
            }
        }
        return path;
    }
}
