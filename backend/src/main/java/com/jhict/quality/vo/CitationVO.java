package com.jhict.quality.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "CitationVO", description = "RAG 引用来源")
public class CitationVO {

    @ApiModelProperty(value = "段落ID")
    private String chunkId;

    @ApiModelProperty(value = "标准ID")
    private String standardId;

    @ApiModelProperty(value = "章节引用")
    private String sectionRef;

    @ApiModelProperty(value = "高亮文本")
    private String highlightText;

    @ApiModelProperty(value = "标准名称")
    private String standardName;
}
