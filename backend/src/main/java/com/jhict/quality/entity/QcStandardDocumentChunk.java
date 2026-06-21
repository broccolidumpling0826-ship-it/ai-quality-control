package com.jhict.quality.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jhict.quality.common.entity.CoreEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("qc_standard_document_chunk")
@ApiModel(value = "QcStandardDocumentChunk", description = "标准文档 RAG 段落")
public class QcStandardDocumentChunk extends CoreEntity {

    @ApiModelProperty(value = "文档ID")
    private String documentId;

    @ApiModelProperty(value = "标准ID")
    private String standardId;

    @ApiModelProperty(value = "段落序号")
    private Integer chunkIndex;

    @ApiModelProperty(value = "章节引用")
    private String sectionRef;

    @ApiModelProperty(value = "段落正文")
    private String chunkText;

    @ApiModelProperty(value = "关键词标签")
    private String keywordTags;
}
