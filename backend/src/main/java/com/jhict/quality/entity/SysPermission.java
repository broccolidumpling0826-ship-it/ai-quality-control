package com.jhict.quality.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jhict.quality.common.entity.CoreEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("sys_permission")
@ApiModel(value = "SysPermission", description = "系统权限")
public class SysPermission extends CoreEntity {

    @ApiModelProperty(value = "权限编码")
    private String permCode;

    @ApiModelProperty(value = "权限名称")
    private String permName;

    @ApiModelProperty(value = "权限类型：MENU/BUTTON/API")
    private String permType;

    @ApiModelProperty(value = "描述")
    private String description;

    @ApiModelProperty(value = "状态：1启用 0禁用")
    private Integer status = 1;
}
