package com.jhict.quality.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel(value = "RagIngestCmd", description = "标准文档入库请求")
public class RagIngestCmd {

    @NotBlank(message = "标准ID不能为空")
    @ApiModelProperty(value = "质量标准ID", required = true)
    private String standardId;

    @NotBlank(message = "文件路径不能为空")
    @ApiModelProperty(value = "文件路径（相对 upload base）", required = true)
    private String filePath;

    @ApiModelProperty(value = "原始文件名")
    private String fileName;
}
