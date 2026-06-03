package com.jhict.quality.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jhict.quality.common.entity.ApiResult;
import com.jhict.quality.dto.SysUserCreateCmd;
import com.jhict.quality.dto.SysUserPageQuery;
import com.jhict.quality.dto.SysUserStatusCmd;
import com.jhict.quality.dto.SysUserUpdateCmd;
import com.jhict.quality.dto.UserAssignRolesCmd;
import com.jhict.quality.entity.SysUser;
import com.jhict.quality.service.api.UserManageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@SaCheckLogin
@RestController
@RequestMapping("/api/v1/admin/users")
@Api(tags = "账号管理（ADMIN）")
public class UserManageController {

    @Resource
    private UserManageService userManageService;

    @PostMapping("/page")
    @ApiOperation(value = "分页查询用户列表")
    @SaCheckRole("ADMIN")
    public ApiResult<IPage<SysUser>> page(@RequestBody SysUserPageQuery query) {
        return ApiResult.success(userManageService.pageUsers(query));
    }

    @PostMapping
    @ApiOperation(value = "新增用户（初始密码 Abc@1234）")
    @SaCheckRole("ADMIN")
    public ApiResult<String> create(@Validated @RequestBody SysUserCreateCmd cmd) {
        String id = userManageService.createUser(cmd);
        return ApiResult.success("新增成功", id);
    }

    @PutMapping("/{id}")
    @ApiOperation(value = "更新用户信息（仅允许修改 username/role/department）")
    @SaCheckRole("ADMIN")
    public ApiResult<Void> update(
            @ApiParam(value = "用户ID", required = true) @PathVariable String id,
            @RequestBody SysUserUpdateCmd cmd) {
        userManageService.updateUser(id, cmd);
        return ApiResult.success();
    }

    @PutMapping("/{id}/status")
    @ApiOperation(value = "启用/禁用用户")
    @SaCheckRole("ADMIN")
    public ApiResult<Void> updateStatus(
            @ApiParam(value = "用户ID", required = true) @PathVariable String id,
            @Validated @RequestBody SysUserStatusCmd cmd) {
        userManageService.updateUserStatus(id, cmd.getStatus());
        return ApiResult.success();
    }

    @PutMapping("/{id}/roles")
    @ApiOperation(value = "分配用户角色（多选）")
    @SaCheckRole("ADMIN")
    public ApiResult<Void> assignRoles(
            @ApiParam(value = "用户ID", required = true) @PathVariable String id,
            @RequestBody UserAssignRolesCmd cmd) {
        userManageService.assignUserRoles(id, cmd.getRoleCodes());
        return ApiResult.success();
    }
}
