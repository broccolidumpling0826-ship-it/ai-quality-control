package com.jhict.quality.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "QcIndicatorItemPageQuery", description = "指标项目分页查询条件")
public class QcIndicatorItemPageQuery {

    @ApiModelProperty(value = "指标名称（模糊匹配）")
    private String indicatorName;

    @ApiModelProperty(value = "指标类别")
    private String category;

    @ApiModelProperty(value = "页码，默认1")
    private Integer pageNum = 1;

    @ApiModelProperty(value = "每页条数，默认20")
    private Integer pageSize = 20;
}
