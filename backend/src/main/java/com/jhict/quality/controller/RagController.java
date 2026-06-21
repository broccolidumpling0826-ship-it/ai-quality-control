package com.jhict.quality.controller;

import com.jhict.quality.ai.rag.DocumentIngestService;
import com.jhict.quality.ai.rag.RagService;
import com.jhict.quality.common.entity.ApiResult;
import com.jhict.quality.entity.QcStandardDocument;
import com.jhict.quality.vo.RagQueryResultVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.Data;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

@RestController
@RequestMapping("/api/v1/rag")
@Api(tags = "标准 RAG 检索")
public class RagController {

    @Resource
    private RagService ragService;

    @Resource
    private DocumentIngestService documentIngestService;

    @PostMapping("/query")
    @ApiOperation(value = "标准条款自然语言检索（RAG 问答）")
    public ApiResult<RagQueryResultVO> query(@Valid @RequestBody RagQueryCmd cmd) {
        RagQueryResultVO result = ragService.query(cmd.getQuestion(), cmd.getStandardId(), cmd.getStandardType());
        return ApiResult.success(result);
    }

    @PostMapping("/ingest")
    @ApiOperation(value = "标准文档入库（Tika 解析 + 分段）")
    public ApiResult<QcStandardDocument> ingest(@Valid @RequestBody RagIngestCmd cmd) {
        QcStandardDocument doc = documentIngestService.ingest(cmd.getStandardId(), cmd.getFilePath(), cmd.getFileName());
        return ApiResult.success(doc);
    }

    @Data
    public static class RagQueryCmd {

        @NotBlank(message = "问题不能为空")
        @ApiModelProperty(value = "查询问题", required = true)
        private String question;

        @ApiModelProperty(value = "限定标准ID（为空则全库检索）")
        private String standardId;

        @ApiModelProperty(value = "限定标准类型（为空则不过滤）")
        private String standardType;
    }

    @Data
    public static class RagIngestCmd {

        @NotBlank(message = "标准ID不能为空")
        @ApiModelProperty(value = "关联标准ID", required = true)
        private String standardId;

        @NotBlank(message = "文件路径不能为空")
        @ApiModelProperty(value = "服务器文件路径或上传相对路径", required = true)
        private String filePath;

        @ApiModelProperty(value = "显示文件名（为空取路径文件名）")
        private String fileName;
    }
}
