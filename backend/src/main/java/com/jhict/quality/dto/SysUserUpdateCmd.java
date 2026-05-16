package com.jhict.quality.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "SysUserUpdateCmd", description = "更新用户命令（仅 username/role/department）")
public class SysUserUpdateCmd {

    @ApiModelProperty(value = "用户名")
    private String username;

    @ApiModelProperty(value = "角色")
    private String role;

    @ApiModelProperty(value = "部门")
    private String department;
}
