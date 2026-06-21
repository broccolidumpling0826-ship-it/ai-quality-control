package com.jhict.quality.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@ApiModel(value = "StandardSourceDocumentVO", description = "标准关联源文档摘要")
public class StandardSourceDocumentVO {

    @ApiModelProperty(value = "源文档ID")
    private String documentId;

    @ApiModelProperty(value = "原始文件名")
    private String sourceFileName;

    @ApiModelProperty(value = "源文件类型扩展名")
    private String sourceFileType;

    @ApiModelProperty(value = "解析状态")
    private String parseStatus;

    @ApiModelProperty(value = "索引状态")
    private String indexStatus;

    @ApiModelProperty(value = "索引时间")
    private LocalDateTime indexedAt;

    @ApiModelProperty(value = "解析或索引错误")
    private String parseErrorMessage;

    @ApiModelProperty(value = "已索引条款数")
    private Integer chunkCount;

    @ApiModelProperty(value = "是否已上传源文件")
    private Boolean hasSourceFile;
}
