package com.jhict.quality.common.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.jhict.quality.common.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
public class FileStorageUtil {

    @Value("${app.upload.base-path}")
    private String basePath;

    private static final DateTimeFormatter YEAR_FORMATTER = DateTimeFormatter.ofPattern("yyyy");
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("MM");

    /**
     * 保存文件
     *
     * @param file     上传的文件
     * @param category 文件分类（如：standards, attachments等）
     * @return 相对路径
     */
    public String saveFile(MultipartFile file, String category) {
        if (file == null || file.isEmpty()) {
            throw new ServiceException("上传文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        LocalDate now = LocalDate.now();
        String year = now.format(YEAR_FORMATTER);
        String month = now.format(MONTH_FORMATTER);
        String uuid = IdUtil.fastSimpleUUID();
        String filename = uuid + "_" + (originalFilename != null ? originalFilename : "file" + extension);

        String relativePath = category + "/" + year + "/" + month + "/" + filename;
        String absolutePath = basePath + File.separator + relativePath.replace("/", File.separator);

        try {
            File targetFile = new File(absolutePath);
            FileUtil.mkParentDirs(targetFile);
            file.transferTo(targetFile);
            log.info("文件保存成功: {}", absolutePath);
            return relativePath;
        } catch (IOException e) {
            log.error("文件保存失败: {}", absolutePath, e);
            throw new ServiceException("文件保存失败: " + e.getMessage());
        }
    }

    /**
     * 获取文件绝对路径
     *
     * @param relativePath 相对路径
     * @return 绝对路径
     */
    public String getAbsolutePath(String relativePath) {
        if (relativePath == null || relativePath.isEmpty()) {
            throw new ServiceException("文件相对路径不能为空");
        }
        return basePath + File.separator + relativePath.replace("/", File.separator);
    }

    /**
     * 删除文件
     *
     * @param relativePath 相对路径
     * @return 是否删除成功
     */
    public boolean deleteFile(String relativePath) {
        if (relativePath == null || relativePath.isEmpty()) {
            log.warn("文件相对路径为空，跳过删除");
            return false;
        }
        String absolutePath = getAbsolutePath(relativePath);
        File file = new File(absolutePath);
        if (!file.exists()) {
            log.warn("文件不存在，跳过删除: {}", absolutePath);
            return false;
        }
        boolean deleted = FileUtil.del(file);
        if (deleted) {
            log.info("文件删除成功: {}", absolutePath);
        } else {
            log.warn("文件删除失败: {}", absolutePath);
        }
        return deleted;
    }
}
