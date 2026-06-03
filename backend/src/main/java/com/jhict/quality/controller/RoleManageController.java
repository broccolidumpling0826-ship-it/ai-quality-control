package com.jhict.quality.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaCheckRole;
import com.jhict.quality.common.entity.ApiResult;
import com.jhict.quality.dto.RoleAssignMenusCmd;
import com.jhict.quality.dto.RoleAssignPermissionsCmd;
import com.jhict.quality.dto.RoleSaveCmd;
import com.jhict.quality.service.api.RoleManageService;
import com.jhict.quality.vo.PermissionVO;
import com.jhict.quality.vo.RoleVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@SaCheckLogin
@RestController
@RequestMapping("/api/v1/admin/roles")
@Api(tags = "角色管理（ADMIN）")
public class RoleManageController {

    @Resource
    private RoleManageService roleManageService;

    @GetMapping
    @ApiOperation(value = "角色列表")
    @SaCheckRole("ADMIN")
    @SaCheckPermission("admin:role:manage")
    public ApiResult<List<RoleVO>> list() {
        return ApiResult.success(roleManageService.listRoles());
    }

    @PostMapping
    @ApiOperation(value = "新增角色")
    @SaCheckRole("ADMIN")
    @SaCheckPermission("admin:role:manage")
    public ApiResult<RoleVO> create(@Validated @RequestBody RoleSaveCmd cmd) {
        return ApiResult.success(roleManageService.createRole(cmd));
    }

    @PutMapping("/{id}")
    @ApiOperation(value = "更新角色")
    @SaCheckRole("ADMIN")
    @SaCheckPermission("admin:role:manage")
    public ApiResult<RoleVO> update(
            @ApiParam(value = "角色ID", required = true) @PathVariable String id,
            @Validated @RequestBody RoleSaveCmd cmd) {
        return ApiResult.success(roleManageService.updateRole(id, cmd));
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除角色")
    @SaCheckRole("ADMIN")
    @SaCheckPermission("admin:role:manage")
    public ApiResult<Void> delete(
            @ApiParam(value = "角色ID", required = true) @PathVariable String id) {
        roleManageService.deleteRole(id);
        return ApiResult.success();
    }

    @GetMapping("/{id}/menus")
    @ApiOperation(value = "获取角色已分配菜单ID")
    @SaCheckRole("ADMIN")
    @SaCheckPermission("admin:role:manage")
    public ApiResult<List<String>> getRoleMenus(
            @ApiParam(value = "角色ID", required = true) @PathVariable String id) {
        return ApiResult.success(roleManageService.getRoleMenuIds(id));
    }

    @PutMapping("/{id}/menus")
    @ApiOperation(value = "分配菜单给角色")
    @SaCheckRole("ADMIN")
    @SaCheckPermission("admin:role:manage")
    public ApiResult<Void> assignMenus(
            @ApiParam(value = "角色ID", required = true) @PathVariable String id,
            @RequestBody RoleAssignMenusCmd cmd) {
        roleManageService.assignMenus(id, cmd.getMenuIds());
        return ApiResult.success();
    }

    @GetMapping("/{id}/permissions")
    @ApiOperation(value = "获取角色已分配权限编码")
    @SaCheckRole("ADMIN")
    @SaCheckPermission("admin:role:manage")
    public ApiResult<List<String>> getRolePermissions(
            @ApiParam(value = "角色ID", required = true) @PathVariable String id) {
        return ApiResult.success(roleManageService.getRolePermissionCodes(id));
    }

    @PutMapping("/{id}/permissions")
    @ApiOperation(value = "分配权限给角色")
    @SaCheckRole("ADMIN")
    @SaCheckPermission("admin:role:manage")
    public ApiResult<Void> assignPermissions(
            @ApiParam(value = "角色ID", required = true) @PathVariable String id,
            @RequestBody RoleAssignPermissionsCmd cmd) {
        roleManageService.assignPermissions(id, cmd.getPermCodes());
        return ApiResult.success();
    }

    @GetMapping("/permissions/all")
    @ApiOperation(value = "全部权限点列表")
    @SaCheckRole("ADMIN")
    @SaCheckPermission("admin:role:manage")
    public ApiResult<List<PermissionVO>> listPermissions() {
        return ApiResult.success(roleManageService.listPermissions());
    }
}
