package com.jhict.quality.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jhict.quality.common.entity.CoreEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("qc_standard_document")
@ApiModel(value = "QcStandardDocument", description = "标准原始文档")
public class QcStandardDocument extends CoreEntity {

    @ApiModelProperty(value = "关联质量标准ID")
    private String standardId;

    @ApiModelProperty(value = "原始文件名")
    private String fileName;

    @ApiModelProperty(value = "存储路径")
    private String filePath;

    @ApiModelProperty(value = "文件类型 PDF/TXT/MD")
    private String fileType;

    @ApiModelProperty(value = "页数")
    private Integer pageCount;

    @ApiModelProperty(value = "入库状态 PENDING/INDEXED/FAILED")
    private String ingestStatus;

    @ApiModelProperty(value = "入库完成时间")
    private String ingestTime;

    @ApiModelProperty(value = "段落数")
    private Integer chunkCount;
}
