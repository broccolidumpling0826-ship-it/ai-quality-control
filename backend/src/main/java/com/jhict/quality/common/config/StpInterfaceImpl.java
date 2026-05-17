package com.jhict.quality.common.config;

import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jhict.quality.common.util.AuthUtils;
import com.jhict.quality.entity.SysUser;
import com.jhict.quality.mapper.SysUserMapper;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

/**
 * Sa-Token 角色/权限提供者，供 @SaCheckRole / @SaCheckPermission 使用。
 * ADMIN 账号返回全部业务角色，从而通过任意 @SaCheckRole 校验。
 */
@Component
public class StpInterfaceImpl implements StpInterface {

    @Resource
    private SysUserMapper sysUserMapper;

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        String role = resolveRole(loginId);
        if (AuthUtils.ROLE_ADMIN.equals(role)) {
            return AuthUtils.allRolesForAdmin();
        }
        if (StringUtils.hasText(role)) {
            return Collections.singletonList(role);
        }
        return Collections.emptyList();
    }

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        if (AuthUtils.ROLE_ADMIN.equals(resolveRole(loginId))) {
            return Collections.singletonList("*");
        }
        return Collections.emptyList();
    }

    private String resolveRole(Object loginId) {
        try {
            Object role = StpUtil.getSession().get("role");
            if (role != null && StringUtils.hasText(role.toString())) {
                return role.toString();
            }
        } catch (Exception ignored) {
            // 会话未建立时回退查库
        }

        SysUser user = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUserNo, String.valueOf(loginId))
                        .eq(SysUser::getStatus, 1)
        );
        if (user != null && StringUtils.hasText(user.getRole())) {
            return user.getRole();
        }
        return "";
    }
}
