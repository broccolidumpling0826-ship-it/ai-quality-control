package com.jhict.quality.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "MenuTreeVO", description = "菜单树节点")
public class MenuTreeVO {

    @ApiModelProperty(value = "菜单ID")
    private String id;

    @ApiModelProperty(value = "父菜单ID")
    private String parentId;

    @ApiModelProperty(value = "菜单类型")
    private String menuType;

    @ApiModelProperty(value = "菜单名称")
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
    private Integer visible;

    @ApiModelProperty(value = "排序")
    private Integer sortOrder;

    @ApiModelProperty(value = "扩展JSON")
    private String metaJson;

    @ApiModelProperty(value = "子菜单")
    @Builder.Default
    private List<MenuTreeVO> children = new ArrayList<>();
}
