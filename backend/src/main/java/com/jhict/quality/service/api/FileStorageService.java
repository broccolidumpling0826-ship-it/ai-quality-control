package com.jhict.quality.service.api;

import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

public interface FileStorageService {

    /**
     * 保存文件
     *
     * @param file     上传的文件
     * @param category 文件分类
     * @return 相对路径
     */
    String save(MultipartFile file, String category);

    /**
     * 获取文件绝对路径
     *
     * @param relativePath 相对路径
     * @return 绝对路径
     */
    String getAbsolutePath(String relativePath);

    /**
     * 输出文件流到 HTTP 响应
     *
     * @param relativePath 相对路径
     * @param response     HTTP 响应
     */
    void serve(String relativePath, HttpServletResponse response);
}
