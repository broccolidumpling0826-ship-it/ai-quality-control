package com.jhict.quality.service.impl;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jhict.quality.common.entity.ApiResult;
import com.jhict.quality.common.exception.ServiceException;
import com.jhict.quality.dto.LoginCmd;
import com.jhict.quality.entity.SysUser;
import com.jhict.quality.mapper.SysUserMapper;
import com.jhict.quality.service.api.AuthService;
import com.jhict.quality.service.api.RbacQueryService;
import com.jhict.quality.vo.LoginVO;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuthServiceImpl implements AuthService {

    @Resource
    private SysUserMapper sysUserMapper;

    @Resource
    private RbacQueryService rbacQueryService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public LoginVO login(LoginCmd cmd) {
        SysUser user = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUserNo, cmd.getUserNo())
                        .eq(SysUser::getStatus, 1)
        );
        if (user == null) {
            throw new ServiceException(ApiResult.CODE_BAD_REQUEST, "用户不存在或已被禁用");
        }

        if (!passwordEncoder.matches(cmd.getPassword(), user.getPassword())) {
            throw new ServiceException(ApiResult.CODE_BAD_REQUEST, "密码错误");
        }

        StpUtil.login(user.getUserNo());

        List<String> roles = rbacQueryService.getUserRoleCodes(user.getUserNo());
        List<String> permissions = rbacQueryService.getUserPermissionCodes(user.getUserNo());

        StpUtil.getSession()
                .set("username", user.getUsername())
                .set("role", user.getRole())
                .set("department", user.getDepartment())
                .set("roles", roles)
                .set("permissions", permissions);

        user.setLastLoginTime(LocalDateTime.now());
        sysUserMapper.updateById(user);

        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
        return buildLoginVO(user, tokenInfo, roles, permissions);
    }

    @Override
    public void logout() {
        StpUtil.logout();
    }

    @Override
    public LoginVO getUserInfo() {
        String userNo = StpUtil.getLoginIdAsString();

        SysUser user = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUserNo, userNo)
                        .eq(SysUser::getStatus, 1)
        );
        if (user == null) {
            throw new ServiceException(ApiResult.CODE_UNAUTHORIZED, "用户不存在或已被禁用");
        }

        List<String> roles = rbacQueryService.getUserRoleCodes(userNo);
        List<String> permissions = rbacQueryService.getUserPermissionCodes(userNo);

        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
        return buildLoginVO(user, tokenInfo, roles, permissions);
    }

    private LoginVO buildLoginVO(SysUser user, SaTokenInfo tokenInfo,
                                 List<String> roles, List<String> permissions) {
        return LoginVO.builder()
                .token(tokenInfo.getTokenValue())
                .tokenName(tokenInfo.getTokenName())
                .userNo(user.getUserNo())
                .username(user.getUsername())
                .role(user.getRole())
                .department(user.getDepartment())
                .roles(roles)
                .permissions(permissions)
                .build();
    }
}
