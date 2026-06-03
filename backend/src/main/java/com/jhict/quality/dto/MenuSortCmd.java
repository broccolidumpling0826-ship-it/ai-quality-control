package com.jhict.quality.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@ApiModel(value = "MenuSortCmd", description = "菜单排序命令")
public class MenuSortCmd {

    @NotBlank(message = "菜单ID不能为空")
    @ApiModelProperty(value = "菜单ID", required = true)
    private String id;

    @NotNull(message = "排序值不能为空")
    @ApiModelProperty(value = "排序", required = true)
    private Integer sortOrder;
}
