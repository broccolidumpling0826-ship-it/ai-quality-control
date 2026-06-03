package com.jhict.quality.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel(value = "RoleAssignMenusCmd", description = "角色分配菜单")
public class RoleAssignMenusCmd {

    @ApiModelProperty(value = "菜单ID列表")
    private List<String> menuIds;
}
