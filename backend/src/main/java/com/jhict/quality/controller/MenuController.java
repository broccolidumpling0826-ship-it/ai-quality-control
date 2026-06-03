package com.jhict.quality.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.jhict.quality.common.entity.ApiResult;
import com.jhict.quality.service.api.MenuService;
import com.jhict.quality.vo.MenuTreeVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@SaCheckLogin
@RestController
@RequestMapping("/api/v1/menus")
@Api(tags = "菜单模块")
public class MenuController {

    @Resource
    private MenuService menuService;

    @GetMapping("/user-tree")
    @ApiOperation(value = "获取当前用户菜单树")
    public ApiResult<List<MenuTreeVO>> getUserMenuTree() {
        String userNo = StpUtil.getLoginIdAsString();
        return ApiResult.success(menuService.getUserMenuTree(userNo));
    }
}
