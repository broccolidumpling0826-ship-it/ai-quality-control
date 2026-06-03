package com.jhict.quality.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel(value = "RoleAssignPermissionsCmd", description = "角色分配权限")
public class RoleAssignPermissionsCmd {

    @ApiModelProperty(value = "权限编码列表")
    private List<String> permCodes;
}
