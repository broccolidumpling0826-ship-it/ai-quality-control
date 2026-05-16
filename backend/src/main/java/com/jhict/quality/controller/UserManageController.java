package com.jhict.quality.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jhict.quality.common.entity.ApiResult;
import com.jhict.quality.dto.SysUserPageQuery;
import com.jhict.quality.entity.SysUser;
import com.jhict.quality.mapper.SysUserMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

@SaCheckLogin
@RestController
@RequestMapping("/api/v1/admin/users")
@Api(tags = "账号管理（ADMIN）")
public class UserManageController {

    private static final String INITIAL_PASSWORD = "Abc@1234";

    @Resource
    private SysUserMapper sysUserMapper;

    @PostMapping("/page")
    @ApiOperation(value = "分页查询用户列表")
    @SaCheckRole("ADMIN")
    public ApiResult<IPage<SysUser>> page(@RequestBody SysUserPageQuery query) {
        int pageNum = query.getPageNum() != null && query.getPageNum() > 0 ? query.getPageNum() : 1;
        int pageSize = query.getPageSize() != null && query.getPageSize() > 0 ? query.getPageSize() : 10;

        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>()
                .like(query.getUserNo() != null && !query.getUserNo().isEmpty(),
                        SysUser::getUserNo, query.getUserNo())
                .like(query.getUsername() != null && !query.getUsername().isEmpty(),
                        SysUser::getUsername, query.getUsername())
                .eq(query.getRole() != null && !query.getRole().isEmpty(),
                        SysUser::getRole, query.getRole())
                .eq(query.getStatus() != null, SysUser::getStatus, query.getStatus())
                .orderByAsc(SysUser::getUserNo);

        IPage<SysUser> pageResult = sysUserMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);

        // Never expose password hashes
        pageResult.getRecords().forEach(u -> u.setPassword(null));

        return ApiResult.success(pageResult);
    }

    @PostMapping
    @ApiOperation(value = "新增用户（初始密码 Abc@1234）")
    @SaCheckRole("ADMIN")
    public ApiResult<String> create(@RequestBody Map<String, String> body) {
        SysUser user = new SysUser();
        user.setUserNo(body.get("userNo"));
        user.setUsername(body.get("username"));
        user.setRole(body.get("role"));
        user.setDepartment(body.get("department"));
        user.setStatus(1);
        user.setPassword(BCrypt.hashpw(INITIAL_PASSWORD, BCrypt.gensalt()));

        sysUserMapper.insert(user);

        return ApiResult.success("新增成功", user.getId());
    }

    @PutMapping("/{id}")
    @ApiOperation(value = "更新用户信息（仅允许修改 username/role/department）")
    @SaCheckRole("ADMIN")
    public ApiResult<Void> update(
            @ApiParam(value = "用户ID", required = true) @PathVariable String id,
            @RequestBody Map<String, String> body) {

        LambdaUpdateWrapper<SysUser> wrapper = new LambdaUpdateWrapper<SysUser>()
                .eq(SysUser::getId, id)
                .set(body.containsKey("username"), SysUser::getUsername, body.get("username"))
                .set(body.containsKey("role"), SysUser::getRole, body.get("role"))
                .set(body.containsKey("department"), SysUser::getDepartment, body.get("department"));

        sysUserMapper.update(null, wrapper);

        return ApiResult.success();
    }

    @PutMapping("/{id}/status")
    @ApiOperation(value = "启用/禁用用户")
    @SaCheckRole("ADMIN")
    public ApiResult<Void> updateStatus(
            @ApiParam(value = "用户ID", required = true) @PathVariable String id,
            @RequestBody Map<String, Integer> body) {

        Integer statusVal = body.get("status");

        LambdaUpdateWrapper<SysUser> wrapper = new LambdaUpdateWrapper<SysUser>()
                .eq(SysUser::getId, id)
                .set(SysUser::getStatus, statusVal);

        sysUserMapper.update(null, wrapper);

        return ApiResult.success();
    }
}
