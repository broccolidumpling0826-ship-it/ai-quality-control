package com.jhict.quality.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel(value = "LoginCmd", description = "登录请求参数")
public class LoginCmd {

    @NotBlank(message = "工号不能为空")
    @ApiModelProperty(value = "工号", required = true)
    private String userNo;

    @NotBlank(message = "密码不能为空")
    @ApiModelProperty(value = "密码", required = true)
    private String password;
}
