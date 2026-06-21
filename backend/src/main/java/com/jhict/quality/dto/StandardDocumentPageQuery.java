package com.jhict.quality.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "StandardDocumentPageQuery", description = "标准源文档分页查询")
public class StandardDocumentPageQuery {

    @ApiModelProperty(value = "关键词（文档编号/名称/标准编号/标准名称）")
    private String keyword;

    @ApiModelProperty(value = "文档类型 STANDARD/AGREEMENT/CASE/COMPLAINT")
    private String documentType;

    @ApiModelProperty(value = "标准类型 NATIONAL/ENTERPRISE/CUSTOMER")
    private String standardType;

    @ApiModelProperty(value = "客户ID")
    private String customerId;

    @ApiModelProperty(value = "品种")
    private String variety;

    @ApiModelProperty(value = "牌号")
    private String grade;

    @ApiModelProperty(value = "解析状态")
    private String parseStatus;

    @ApiModelProperty(value = "索引状态")
    private String indexStatus;

    @ApiModelProperty(value = "状态")
    private String status;

    @ApiModelProperty(value = "页码，默认1")
    private Integer pageNum = 1;

    @ApiModelProperty(value = "每页条数，默认20")
    private Integer pageSize = 20;
}
