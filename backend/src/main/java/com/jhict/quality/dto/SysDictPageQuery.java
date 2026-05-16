package com.jhict.quality.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "SysDictPageQuery", description = "字典分类分页查询")
public class SysDictPageQuery {

    @ApiModelProperty(value = "字典编码（模糊）")
    private String dictCode;

    @ApiModelProperty(value = "字典名称（模糊）")
    private String dictName;

    @ApiModelProperty(value = "页码，默认1")
    private Integer pageNum = 1;

    @ApiModelProperty(value = "每页条数，默认20")
    private Integer pageSize = 20;
}
