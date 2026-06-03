package com.jhict.quality.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel(value = "RoleSaveCmd", description = "角色保存命令")
public class RoleSaveCmd {

    @NotBlank(message = "角色编码不能为空")
    @ApiModelProperty(value = "角色编码", required = true)
    private String roleCode;

    @NotBlank(message = "角色名称不能为空")
    @ApiModelProperty(value = "角色名称", required = true)
    private String roleName;

    @ApiModelProperty(value = "描述")
    private String description;

    @ApiModelProperty(value = "排序")
    private Integer sortOrder = 0;

    @ApiModelProperty(value = "状态")
    private Integer status = 1;
}
