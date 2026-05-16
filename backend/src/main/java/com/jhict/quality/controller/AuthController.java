package com.jhict.quality.controller;

import com.jhict.quality.common.entity.ApiResult;
import com.jhict.quality.dto.LoginCmd;
import com.jhict.quality.service.api.AuthService;
import com.jhict.quality.vo.LoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/v1/auth")
@Api(tags = "认证模块")
public class AuthController {

    @Resource
    private AuthService authService;

    @PostMapping("/login")
    @ApiOperation(value = "用户登录")
    public ApiResult<LoginVO> login(@Validated @RequestBody LoginCmd cmd) {
        return ApiResult.success(authService.login(cmd));
    }

    @PostMapping("/logout")
    @ApiOperation(value = "退出登录")
    public ApiResult<String> logout() {
        authService.logout();
        return ApiResult.success("退出成功", null);
    }

    @GetMapping("/user/info")
    @ApiOperation(value = "获取当前登录用户信息")
    public ApiResult<LoginVO> getUserInfo() {
        return ApiResult.success(authService.getUserInfo());
    }
}
