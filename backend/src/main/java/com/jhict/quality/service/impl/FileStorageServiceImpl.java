package com.jhict.quality.service.impl;

import com.jhict.quality.common.exception.ServiceException;
import com.jhict.quality.common.util.FileStorageUtil;
import com.jhict.quality.service.api.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Service
public class FileStorageServiceImpl implements FileStorageService {

    @Resource
    private FileStorageUtil fileStorageUtil;

    @Override
    public String save(MultipartFile file, String category) {
        return fileStorageUtil.saveFile(file, category);
    }

    @Override
    public String getAbsolutePath(String relativePath) {
        return fileStorageUtil.getAbsolutePath(relativePath);
    }

    @Override
    public void serve(String relativePath, HttpServletResponse response) {
        if (!StringUtils.hasText(relativePath)) {
            throw new ServiceException("文件路径不能为空");
        }

        String absolutePath = fileStorageUtil.getAbsolutePath(relativePath);
        File file = new File(absolutePath);

        if (!file.exists() || !file.isFile()) {
            throw new ServiceException("文件不存在: " + relativePath);
        }

        // 推断 Content-Type
        String contentType = detectContentType(absolutePath);
        response.setContentType(contentType);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        // 提取原始文件名（去掉 UUID 前缀）
        String filename = file.getName();
        int underscoreIdx = filename.indexOf('_');
        String displayName = (underscoreIdx >= 0 && underscoreIdx < filename.length() - 1)
                ? filename.substring(underscoreIdx + 1)
                : filename;

        try {
            String encodedName = URLEncoder.encode(displayName, StandardCharsets.UTF_8.name())
                    .replace("+", "%20");
            response.setHeader("Content-Disposition",
                    "inline; filename=\"" + encodedName + "\"; filename*=UTF-8''" + encodedName);
            response.setContentLengthLong(file.length());
        } catch (Exception e) {
            log.warn("Content-Disposition 设置失败", e);
        }

        try (FileInputStream fis = new FileInputStream(file);
             OutputStream out = response.getOutputStream()) {
            byte[] buffer = new byte[8192];
            int len;
            while ((len = fis.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
            out.flush();
        } catch (IOException e) {
            log.error("文件输出失败，path={}", absolutePath, e);
            throw new ServiceException("文件读取失败");
        }
    }

    private String detectContentType(String absolutePath) {
        try {
            Path path = Paths.get(absolutePath);
            String detected = Files.probeContentType(path);
            if (StringUtils.hasText(detected)) {
                return detected;
            }
        } catch (IOException e) {
            log.debug("Content-Type 探测失败，path={}", absolutePath);
        }
        // 根据扩展名简单映射
        String lower = absolutePath.toLowerCase();
        if (lower.endsWith(".pdf")) return MediaType.APPLICATION_PDF_VALUE;
        if (lower.endsWith(".png")) return MediaType.IMAGE_PNG_VALUE;
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return MediaType.IMAGE_JPEG_VALUE;
        if (lower.endsWith(".gif")) return MediaType.IMAGE_GIF_VALUE;
        if (lower.endsWith(".xlsx") || lower.endsWith(".xls"))
            return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        if (lower.endsWith(".docx") || lower.endsWith(".doc"))
            return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        return MediaType.APPLICATION_OCTET_STREAM_VALUE;
    }
}
