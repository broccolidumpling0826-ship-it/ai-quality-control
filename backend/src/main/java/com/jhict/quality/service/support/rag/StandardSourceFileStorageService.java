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
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;

@Service
public class StandardSourceFileStorageService {

    private static final Set<String> DEFAULT_ALLOWED_EXTENSIONS = new HashSet<>(
            Arrays.asList("pdf", "xlsx", "xls", "png", "jpg", "jpeg"));

    @Value("${app.standard-document.storage-path:resources/standard-documents}")
    private String storagePath;

    @Value("${app.standard-document.max-file-size-mb:50}")
    private long maxFileSizeMb;

    @Value("${app.standard-document.allowed-extensions:pdf,xlsx,xls,png,jpg,jpeg}")
    private String allowedExtensions;

    @Value("${app.standard-document.max-files-per-standard:10}")
    private int maxFilesPerStandard;

    public void validateSourceFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ServiceException(ApiResult.CODE_BAD_REQUEST, "请上传标准源文件");
        }
        String extension = extensionOf(file.getOriginalFilename());
        if (!isAllowedExtension(extension)) {
            throw new ServiceException(ApiResult.CODE_BAD_REQUEST,
                    "仅支持 PDF、Excel（xlsx/xls）或图片（png/jpg/jpeg）标准源文件");
        }
        long maxBytes = maxFileSizeMb * 1024L * 1024L;
        if (file.getSize() > maxBytes) {
            throw new ServiceException(ApiResult.CODE_BAD_REQUEST, "源文件大小不能超过 " + maxFileSizeMb + "MB");
        }
    }

    public void assertCanAddFile(int currentFileCount) {
        if (currentFileCount >= maxFilesPerStandard) {
            throw new ServiceException(ApiResult.CODE_BAD_REQUEST,
                    "每个标准最多上传 " + maxFilesPerStandard + " 个源文件");
        }
    }

    public StoredStandardFile save(String standardId, String documentId, MultipartFile file) {
        validateSourceFile(file);
        if (!StringUtils.hasText(standardId)) {
            throw new ServiceException(ApiResult.CODE_BAD_REQUEST, "标准ID不能为空");
        }
        if (!StringUtils.hasText(documentId)) {
            throw new ServiceException(ApiResult.CODE_BAD_REQUEST, "源文档ID不能为空");
        }
        try {
            Path standardDir = resolveStandardDir(standardId);
            Files.createDirectories(standardDir);

            String safeName = sanitizeFileName(file.getOriginalFilename());
            String storedName = documentId + "_" + safeName;
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
            return new StoredStandardFile(safeName, relativePath, hash, target, extensionOf(safeName));
        } catch (ServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ServiceException(ApiResult.CODE_SERVER_ERROR, "保存标准源文件失败: " + ex.getMessage());
        }
    }

    public void deleteStoredFile(String relativePath) {
        if (!StringUtils.hasText(relativePath)) {
            return;
        }
        try {
            Path path = resolveReadablePath(relativePath);
            Files.deleteIfExists(path);
        } catch (ServiceException ex) {
            if (ex.getCode() == ApiResult.CODE_NOT_FOUND) {
                return;
            }
            throw ex;
        } catch (IOException ex) {
            throw new ServiceException(ApiResult.CODE_SERVER_ERROR, "删除标准源文件失败: " + ex.getMessage());
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

    public String contentTypeForExtension(String extension) {
        if (!StringUtils.hasText(extension)) {
            return "application/octet-stream";
        }
        switch (extension.toLowerCase(Locale.ROOT)) {
            case "pdf":
                return "application/pdf";
            case "xlsx":
                return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "xls":
                return "application/vnd.ms-excel";
            case "png":
                return "image/png";
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            default:
                return "application/octet-stream";
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

    private String sanitizeFileName(String originalName) {
        String name = originalName.replace("\\", "/");
        int slash = name.lastIndexOf('/');
        if (slash >= 0) {
            name = name.substring(slash + 1);
        }
        name = name.replaceAll("[^a-zA-Z0-9._\\-\\u4e00-\\u9fa5]", "_");
        String extension = extensionOf(name);
        if (!StringUtils.hasText(extension)) {
            throw new ServiceException(ApiResult.CODE_BAD_REQUEST, "源文件缺少有效扩展名");
        }
        if (!isAllowedExtension(extension)) {
            throw new ServiceException(ApiResult.CODE_BAD_REQUEST, "不支持的源文件扩展名: " + extension);
        }
        if (!name.toLowerCase(Locale.ROOT).endsWith("." + extension)) {
            name = name + "." + extension;
        }
        return name;
    }

    private boolean isAllowedExtension(String extension) {
        return StringUtils.hasText(extension) && allowedExtensionSet().contains(extension.toLowerCase(Locale.ROOT));
    }

    private Set<String> allowedExtensionSet() {
        if (!StringUtils.hasText(allowedExtensions)) {
            return DEFAULT_ALLOWED_EXTENSIONS;
        }
        Set<String> extensions = new HashSet<>();
        for (String item : allowedExtensions.split(",")) {
            if (StringUtils.hasText(item)) {
                extensions.add(item.trim().toLowerCase(Locale.ROOT));
            }
        }
        return extensions.isEmpty() ? DEFAULT_ALLOWED_EXTENSIONS : extensions;
    }

    private String extensionOf(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            return "";
        }
        int index = fileName.lastIndexOf('.');
        if (index < 0 || index == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(index + 1).toLowerCase(Locale.ROOT);
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
        private final String fileExtension;

        public StoredStandardFile(String originalFileName, String relativePath, String fileHash,
                                  Path absolutePath, String fileExtension) {
            this.originalFileName = originalFileName;
            this.relativePath = relativePath;
            this.fileHash = fileHash;
            this.absolutePath = absolutePath;
            this.fileExtension = fileExtension;
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

        public String getFileExtension() {
            return fileExtension;
        }
    }
}
