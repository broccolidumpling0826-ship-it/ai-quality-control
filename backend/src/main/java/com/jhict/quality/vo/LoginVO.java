package com.jhict.quality.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "LoginVO", description = "登录响应信息")
public class LoginVO {

    @ApiModelProperty(value = "Token 值")
    private String token;

    @ApiModelProperty(value = "Token 名称（Header Key）")
    private String tokenName;

    @ApiModelProperty(value = "工号")
    private String userNo;

    @ApiModelProperty(value = "用户名")
    private String username;

    @ApiModelProperty(value = "角色")
    private String role;

    @ApiModelProperty(value = "部门")
    private String department;

    @ApiModelProperty(value = "角色列表")
    private List<String> roles;

    @ApiModelProperty(value = "权限列表")
    private List<String> permissions;
}
