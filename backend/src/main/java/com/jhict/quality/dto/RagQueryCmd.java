package com.jhict.quality.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel(value = "RagQueryCmd", description = "RAG 问答请求")
public class RagQueryCmd {

    @NotBlank(message = "问题不能为空")
    @ApiModelProperty(value = "自然语言问题", required = true)
    private String question;

    @ApiModelProperty(value = "标准类型过滤 NATIONAL/CUSTOMER/ENTERPRISE")
    private String standardType;

    @ApiModelProperty(value = "标准ID过滤")
    private String standardId;
}
