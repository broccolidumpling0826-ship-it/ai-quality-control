package com.jhict.quality.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jhict.quality.common.entity.CoreEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("sys_menu")
@ApiModel(value = "SysMenu", description = "系统菜单")
public class SysMenu extends CoreEntity {

    @ApiModelProperty(value = "父菜单ID，0为根")
    private String parentId = "0";

    @ApiModelProperty(value = "菜单类型：DIR/MENU/HIDDEN/LINK/IFRAME")
    private String menuType;

    @ApiModelProperty(value = "菜单名称")
    private String menuName;

    @ApiModelProperty(value = "路由路径")
    private String path;

    @ApiModelProperty(value = "Vue组件相对路径")
    private String component;

    @ApiModelProperty(value = "Vue Router name")
    private String routeName;

    @ApiModelProperty(value = "图标")
    private String icon;

    @ApiModelProperty(value = "关联权限编码")
    private String permCode;

    @ApiModelProperty(value = "侧栏是否可见")
    private Integer visible = 1;

    @ApiModelProperty(value = "排序")
    private Integer sortOrder = 0;

    @ApiModelProperty(value = "状态：1启用 0禁用")
    private Integer status = 1;

    @ApiModelProperty(value = "扩展JSON")
    private String metaJson;
}
