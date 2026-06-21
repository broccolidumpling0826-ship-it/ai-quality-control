package com.jhict.quality.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "StandardClausePageQuery", description = "标准源条款分页查询")
public class StandardClausePageQuery {

    @ApiModelProperty(value = "源文档ID")
    private String documentId;

    @ApiModelProperty(value = "结构化标准ID")
    private String standardId;

    @ApiModelProperty(value = "关键词（条款号/段落/标准编号/指标）")
    private String keyword;

    @ApiModelProperty(value = "来源类型 NATIONAL/ENTERPRISE/CUSTOMER/CASE/COMPLAINT")
    private String sourceType;

    @ApiModelProperty(value = "客户ID")
    private String customerId;

    @ApiModelProperty(value = "品种")
    private String variety;

    @ApiModelProperty(value = "牌号")
    private String grade;

    @ApiModelProperty(value = "指标代码")
    private String indicatorCode;

    @ApiModelProperty(value = "向量状态")
    private String embeddingStatus;

    @ApiModelProperty(value = "页码，默认1")
    private Integer pageNum = 1;

    @ApiModelProperty(value = "每页条数，默认20")
    private Integer pageSize = 20;
}
