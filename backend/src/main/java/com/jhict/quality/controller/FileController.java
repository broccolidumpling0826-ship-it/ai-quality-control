package com.jhict.quality.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.jhict.quality.common.entity.ApiResult;
import com.jhict.quality.service.api.FileStorageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

@SaCheckLogin
@RestController
@RequestMapping("/api/v1/files")
@Api(tags = "文件管理")
public class FileController {

    @Resource
    private FileStorageService fileStorageService;

    @PostMapping("/upload")
    @ApiOperation(value = "上传文件，返回相对路径")
    public ApiResult<String> upload(
            @ApiParam(value = "文件", required = true) @RequestPart("file") MultipartFile file,
            @ApiParam(value = "文件分类（如：standards/attachments/concession-confirm）", required = true)
            @RequestParam String category) {
        String relativePath = fileStorageService.save(file, category);
        return ApiResult.success("文件上传成功", relativePath);
    }

    @GetMapping("/{category}/{year}/{month}/{filename}")
    @ApiOperation(value = "下载/预览文件（需登录）")
    public void serve(
            @ApiParam(value = "文件分类") @PathVariable String category,
            @ApiParam(value = "年份") @PathVariable String year,
            @ApiParam(value = "月份") @PathVariable String month,
            @ApiParam(value = "文件名") @PathVariable String filename,
            HttpServletResponse response) {
        String relativePath = category + "/" + year + "/" + month + "/" + filename;
        fileStorageService.serve(relativePath, response);
    }
}
