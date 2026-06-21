package com.jhict.quality.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "StandardDocumentIngestCmd", description = "标准源文档解析切片并索引命令")
public class StandardDocumentIngestCmd {

    @ApiModelProperty(value = "源文档ID", required = true)
    private String documentId;

    @ApiModelProperty(value = "是否重新解析并替换已有条款，默认true")
    private Boolean reindexExisting = Boolean.TRUE;

    @ApiModelProperty(value = "目标ES索引名，为空使用默认配置")
    private String indexName;

    @ApiModelProperty(value = "最大切片字符数，默认1200")
    private Integer maxChunkChars;
}
