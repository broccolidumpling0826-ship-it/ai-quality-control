package com.jhict.quality.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel(value = "SysUserCreateCmd", description = "新增用户命令")
public class SysUserCreateCmd {

    @NotBlank(message = "工号不能为空")
    @ApiModelProperty(value = "工号", required = true)
    private String userNo;

    @NotBlank(message = "用户名不能为空")
    @ApiModelProperty(value = "用户名", required = true)
    private String username;

    @NotBlank(message = "角色不能为空")
    @ApiModelProperty(value = "角色", required = true)
    private String role;

    @ApiModelProperty(value = "部门")
    private String department;
}
