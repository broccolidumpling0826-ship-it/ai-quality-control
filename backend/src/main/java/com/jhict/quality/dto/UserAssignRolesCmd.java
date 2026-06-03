package com.jhict.quality.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel(value = "UserAssignRolesCmd", description = "用户分配角色")
public class UserAssignRolesCmd {

    @ApiModelProperty(value = "角色编码列表")
    private List<String> roleCodes;
}
