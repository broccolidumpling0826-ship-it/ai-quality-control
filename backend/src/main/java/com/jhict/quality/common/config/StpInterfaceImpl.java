package com.jhict.quality.common.config;

import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jhict.quality.entity.SysUser;
import com.jhict.quality.mapper.SysUserMapper;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

/**
 * Sa-Token 角色/权限提供者，供 @SaCheckRole / @SaCheckPermission 使用。
 */
@Component
public class StpInterfaceImpl implements StpInterface {

    @Resource
    private SysUserMapper sysUserMapper;

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        try {
            Object role = StpUtil.getSession().get("role");
            if (role != null && StringUtils.hasText(role.toString())) {
                return Collections.singletonList(role.toString());
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
            return Collections.singletonList(user.getRole());
        }
        return Collections.emptyList();
    }

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return Collections.emptyList();
    }
}
