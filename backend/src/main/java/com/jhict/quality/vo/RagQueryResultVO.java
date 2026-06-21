package com.jhict.quality.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel(value = "RagQueryResultVO", description = "RAG 问答结果")
public class RagQueryResultVO {

    @ApiModelProperty(value = "是否找到")
    private Boolean found;

    @ApiModelProperty(value = "回答内容")
    private String answer;

    @ApiModelProperty(value = "引用列表")
    private List<CitationVO> citations;

    @ApiModelProperty(value = "是否降级")
    private Boolean degraded;

    @ApiModelProperty(value = "审计日志ID")
    private String auditLogId;
}
