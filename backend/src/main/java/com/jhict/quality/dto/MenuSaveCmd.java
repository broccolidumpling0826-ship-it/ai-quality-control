package com.jhict.quality.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel(value = "MenuSaveCmd", description = "菜单保存命令")
public class MenuSaveCmd {

    @ApiModelProperty(value = "父菜单ID")
    private String parentId = "0";

    @NotBlank(message = "菜单类型不能为空")
    @ApiModelProperty(value = "菜单类型", required = true)
    private String menuType;

    @NotBlank(message = "菜单名称不能为空")
    @ApiModelProperty(value = "菜单名称", required = true)
    private String menuName;

    @ApiModelProperty(value = "路由路径")
    private String path;

    @ApiModelProperty(value = "组件路径")
    private String component;

    @ApiModelProperty(value = "路由名称")
    private String routeName;

    @ApiModelProperty(value = "图标")
    private String icon;

    @ApiModelProperty(value = "权限编码")
    private String permCode;

    @ApiModelProperty(value = "是否可见")
    private Integer visible = 1;

    @ApiModelProperty(value = "排序")
    private Integer sortOrder = 0;

    @ApiModelProperty(value = "状态")
    private Integer status = 1;

    @ApiModelProperty(value = "扩展JSON")
    private String metaJson;
}
