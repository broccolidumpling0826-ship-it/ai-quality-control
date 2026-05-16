package com.jhict.quality.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@ApiModel(value = "SysUserStatusCmd", description = "启用/禁用用户命令")
public class SysUserStatusCmd {

    @NotNull(message = "状态不能为空")
    @ApiModelProperty(value = "状态：1启用 0禁用", required = true)
    private Integer status;
}
