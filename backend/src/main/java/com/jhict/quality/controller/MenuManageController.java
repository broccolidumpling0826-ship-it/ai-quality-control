package com.jhict.quality.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaCheckRole;
import com.jhict.quality.common.entity.ApiResult;
import com.jhict.quality.dto.MenuSaveCmd;
import com.jhict.quality.dto.MenuSortCmd;
import com.jhict.quality.service.api.MenuManageService;
import com.jhict.quality.vo.MenuTreeVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@SaCheckLogin
@RestController
@RequestMapping("/api/v1/admin/menus")
@Api(tags = "菜单管理（ADMIN）")
public class MenuManageController {

    @Resource
    private MenuManageService menuManageService;

    @GetMapping("/tree")
    @ApiOperation(value = "获取完整菜单树")
    @SaCheckRole("ADMIN")
    @SaCheckPermission("admin:menu:manage")
    public ApiResult<List<MenuTreeVO>> getFullTree() {
        return ApiResult.success(menuManageService.getFullMenuTree());
    }

    @PostMapping
    @ApiOperation(value = "新增菜单")
    @SaCheckRole("ADMIN")
    @SaCheckPermission("admin:menu:manage")
    public ApiResult<MenuTreeVO> create(@Validated @RequestBody MenuSaveCmd cmd) {
        return ApiResult.success(menuManageService.createMenu(cmd));
    }

    @PutMapping("/{id}")
    @ApiOperation(value = "更新菜单")
    @SaCheckRole("ADMIN")
    @SaCheckPermission("admin:menu:manage")
    public ApiResult<MenuTreeVO> update(
            @ApiParam(value = "菜单ID", required = true) @PathVariable String id,
            @Validated @RequestBody MenuSaveCmd cmd) {
        return ApiResult.success(menuManageService.updateMenu(id, cmd));
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除菜单")
    @SaCheckRole("ADMIN")
    @SaCheckPermission("admin:menu:manage")
    public ApiResult<Void> delete(
            @ApiParam(value = "菜单ID", required = true) @PathVariable String id) {
        menuManageService.deleteMenu(id);
        return ApiResult.success();
    }

    @PutMapping("/sort")
    @ApiOperation(value = "批量更新菜单排序")
    @SaCheckRole("ADMIN")
    @SaCheckPermission("admin:menu:manage")
    public ApiResult<Void> batchSort(@RequestBody List<MenuSortCmd> cmds) {
        menuManageService.batchSort(cmds);
        return ApiResult.success();
    }
}
