package com.jhict.quality.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jhict.quality.common.entity.CoreEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@TableName("sys_user")
@ApiModel(value = "SysUser", description = "系统用户")
public class SysUser extends CoreEntity {

    @ApiModelProperty(value = "工号")
    private String userNo;

    @ApiModelProperty(value = "用户名")
    private String username;

    @ApiModelProperty(value = "密码（BCrypt加密）")
    private String password;

    @ApiModelProperty(value = "角色")
    private String role;

    @ApiModelProperty(value = "部门")
    private String department;

    @ApiModelProperty(value = "状态：1启用 0禁用")
    private Integer status = 1;

    @ApiModelProperty(value = "最后登录时间")
    private LocalDateTime lastLoginTime;
}
