package com.jhict.quality.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "SysUserPageQuery", description = "用户分页查询条件")
public class SysUserPageQuery {

    @ApiModelProperty(value = "工号（模糊）")
    private String userNo;

    @ApiModelProperty(value = "用户名（模糊）")
    private String username;

    @ApiModelProperty(value = "角色")
    private String role;

    @ApiModelProperty(value = "状态：1启用 0禁用")
    private Integer status;

    @ApiModelProperty(value = "页码，默认1")
    private Integer pageNum = 1;

    @ApiModelProperty(value = "每页条数，默认10")
    private Integer pageSize = 10;
}
