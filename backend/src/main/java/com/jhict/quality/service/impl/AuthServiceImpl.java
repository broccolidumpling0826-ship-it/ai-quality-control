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
import com.jhict.quality.vo.LoginVO;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;

@Service
public class AuthServiceImpl implements AuthService {

    @Resource
    private SysUserMapper sysUserMapper;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public LoginVO login(LoginCmd cmd) {
        // 查询用户
        SysUser user = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUserNo, cmd.getUserNo())
                        .eq(SysUser::getStatus, 1)
        );
        if (user == null) {
            throw new ServiceException(ApiResult.CODE_BAD_REQUEST, "用户不存在或已被禁用");
        }

        // 校验密码
        if (!passwordEncoder.matches(cmd.getPassword(), user.getPassword())) {
            throw new ServiceException(ApiResult.CODE_BAD_REQUEST, "密码错误");
        }

        // Sa-Token 登录，以工号作为登录 ID（角色由 StpInterfaceImpl 从 Session/DB 读取）
        StpUtil.login(user.getUserNo());

        // 将用户信息存入 Session（@SaCheckRole 依赖 StpInterfaceImpl.getRoleList）
        StpUtil.getSession()
                .set("username", user.getUsername())
                .set("role", user.getRole())
                .set("department", user.getDepartment());

        // 更新最后登录时间
        user.setLastLoginTime(LocalDateTime.now());
        sysUserMapper.updateById(user);

        // 获取 Token 信息
        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();

        return LoginVO.builder()
                .token(tokenInfo.getTokenValue())
                .tokenName(tokenInfo.getTokenName())
                .userNo(user.getUserNo())
                .username(user.getUsername())
                .role(user.getRole())
                .department(user.getDepartment())
                .build();
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

        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();

        return LoginVO.builder()
                .token(tokenInfo.getTokenValue())
                .tokenName(tokenInfo.getTokenName())
                .userNo(user.getUserNo())
                .username(user.getUsername())
                .role(user.getRole())
                .department(user.getDepartment())
                .build();
    }
}
