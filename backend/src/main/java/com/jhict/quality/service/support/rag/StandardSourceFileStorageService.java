package com.jhict.quality.service.support.rag;

import com.jhict.quality.common.entity.ApiResult;
import com.jhict.quality.common.exception.ServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.Comparator;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Stream;

@Service
public class StandardSourceFileStorageService {

    private static final String PDF_SUFFIX = ".pdf";

    @Value("${app.standard-document.storage-path:resources/standard-documents}")
    private String storagePath;

    @Value("${app.standard-document.max-file-size-mb:50}")
    private long maxFileSizeMb;

    public void validatePdf(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ServiceException(ApiResult.CODE_BAD_REQUEST, "请上传 PDF 文件");
        }
        String originalName = file.getOriginalFilename();
        if (!StringUtils.hasText(originalName) || !originalName.toLowerCase(Locale.ROOT).endsWith(PDF_SUFFIX)) {
            throw new ServiceException(ApiResult.CODE_BAD_REQUEST, "仅支持 PDF 标准源文件");
        }
        long maxBytes = maxFileSizeMb * 1024L * 1024L;
        if (file.getSize() > maxBytes) {
            throw new ServiceException(ApiResult.CODE_BAD_REQUEST, "PDF 文件大小不能超过 " + maxFileSizeMb + "MB");
        }
    }

    public StoredStandardFile save(String standardId, MultipartFile file) {
        validatePdf(file);
        if (!StringUtils.hasText(standardId)) {
            throw new ServiceException(ApiResult.CODE_BAD_REQUEST, "标准ID不能为空");
        }
        try {
            Path standardDir = resolveStandardDir(standardId);
            Files.createDirectories(standardDir);
            clearDirectory(standardDir);

            String safeName = sanitizeFileName(file.getOriginalFilename());
            String storedName = UUID.randomUUID().toString().replace("-", "") + "_" + safeName;
            Path target = standardDir.resolve(storedName);

            String hash;
            try (InputStream inputStream = file.getInputStream()) {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                try (DigestInputStream dis = new DigestInputStream(inputStream, digest)) {
                    Files.copy(dis, target, StandardCopyOption.REPLACE_EXISTING);
                }
                hash = toHex(digest.digest());
            }

            String relativePath = toRelativePath(target);
            return new StoredStandardFile(safeName, relativePath, hash, target);
        } catch (ServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ServiceException(ApiResult.CODE_SERVER_ERROR, "保存标准 PDF 失败: " + ex.getMessage());
        }
    }

    public Path resolveReadablePath(String relativePath) {
        if (!StringUtils.hasText(relativePath)) {
            throw new ServiceException(ApiResult.CODE_NOT_FOUND, "标准源文件不存在");
        }
        Path path = Paths.get(relativePath).normalize();
        if (Files.exists(path) && Files.isRegularFile(path)) {
            return path;
        }
        Path fromBackend = Paths.get(".").resolve(relativePath).normalize();
        if (Files.exists(fromBackend) && Files.isRegularFile(fromBackend)) {
            return fromBackend;
        }
        throw new ServiceException(ApiResult.CODE_NOT_FOUND, "标准源文件不存在: " + relativePath);
    }

    public void deleteStandardDirectory(String standardId) {
        if (!StringUtils.hasText(standardId)) {
            return;
        }
        Path standardDir = resolveStandardDir(standardId);
        if (!Files.exists(standardDir)) {
            return;
        }
        try (Stream<Path> walk = Files.walk(standardDir)) {
            walk.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException ignored) {
                    // best effort cleanup
                }
            });
        } catch (IOException ex) {
            throw new ServiceException(ApiResult.CODE_SERVER_ERROR, "删除标准源文件目录失败: " + ex.getMessage());
        }
    }

    private Path resolveStandardDir(String standardId) {
        return Paths.get(storagePath, standardId).normalize().toAbsolutePath();
    }

    private String toRelativePath(Path absolutePath) {
        Path cwd = Paths.get(".").toAbsolutePath().normalize();
        Path normalizedTarget = absolutePath.normalize().toAbsolutePath();
        if (normalizedTarget.startsWith(cwd)) {
            return cwd.relativize(normalizedTarget).toString().replace('\\', '/');
        }
        return Paths.get(storagePath, normalizedTarget.getFileName().toString()).toString().replace('\\', '/');
    }

    private void clearDirectory(Path dir) throws IOException {
        if (!Files.exists(dir)) {
            return;
        }
        try (Stream<Path> walk = Files.list(dir)) {
            for (Path path : (Iterable<Path>) walk::iterator) {
                Files.deleteIfExists(path);
            }
        }
    }

    private String sanitizeFileName(String originalName) {
        String name = originalName.replace("\\", "/");
        int slash = name.lastIndexOf('/');
        if (slash >= 0) {
            name = name.substring(slash + 1);
        }
        name = name.replaceAll("[^a-zA-Z0-9._\\-\\u4e00-\\u9fa5]", "_");
        if (!name.toLowerCase(Locale.ROOT).endsWith(PDF_SUFFIX)) {
            name = name + PDF_SUFFIX;
        }
        return name;
    }

    private String toHex(byte[] digest) {
        StringBuilder builder = new StringBuilder(digest.length * 2);
        for (byte value : digest) {
            builder.append(String.format("%02x", value));
        }
        return builder.toString();
    }

    public static class StoredStandardFile {
        private final String originalFileName;
        private final String relativePath;
        private final String fileHash;
        private final Path absolutePath;

        public StoredStandardFile(String originalFileName, String relativePath, String fileHash, Path absolutePath) {
            this.originalFileName = originalFileName;
            this.relativePath = relativePath;
            this.fileHash = fileHash;
            this.absolutePath = absolutePath;
        }

        public String getOriginalFileName() {
            return originalFileName;
        }

        public String getRelativePath() {
            return relativePath;
        }

        public String getFileHash() {
            return fileHash;
        }

        public Path getAbsolutePath() {
            return absolutePath;
        }
    }
}
