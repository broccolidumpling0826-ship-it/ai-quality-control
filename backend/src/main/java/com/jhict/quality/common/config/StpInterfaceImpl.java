package com.jhict.quality.common.config;

import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpUtil;
import com.jhict.quality.common.util.AuthUtils;
import com.jhict.quality.service.api.RbacQueryService;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

/**
 * Sa-Token 角色/权限提供者，供 @SaCheckRole / @SaCheckPermission 使用。
 * ADMIN 账号返回全部业务角色与通配符权限。
 */
@Component
public class StpInterfaceImpl implements StpInterface {

    @Resource
    private RbacQueryService rbacQueryService;

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        String userNo = String.valueOf(loginId);
        List<String> roles = rbacQueryService.getUserRoleCodes(userNo);
        if (!CollectionUtils.isEmpty(roles) && roles.contains(AuthUtils.ROLE_ADMIN)) {
            return AuthUtils.allRolesForAdmin();
        }
        if (!CollectionUtils.isEmpty(roles)) {
            return roles;
        }

        String legacyRole = resolveLegacyRole();
        if (AuthUtils.ROLE_ADMIN.equals(legacyRole)) {
            return AuthUtils.allRolesForAdmin();
        }
        if (StringUtils.hasText(legacyRole)) {
            return Collections.singletonList(legacyRole);
        }
        return Collections.emptyList();
    }

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        String userNo = String.valueOf(loginId);
        List<String> permissions = rbacQueryService.getUserPermissionCodes(userNo);
        if (!CollectionUtils.isEmpty(permissions)) {
            return permissions;
        }

        if (AuthUtils.ROLE_ADMIN.equals(resolveLegacyRole())) {
            return Collections.singletonList("*");
        }
        return Collections.emptyList();
    }

    private String resolveLegacyRole() {
        try {
            Object role = StpUtil.getSession().get("role");
            if (role != null && StringUtils.hasText(role.toString())) {
                return role.toString();
            }
        } catch (Exception ignored) {
            // 会话未建立
        }
        return "";
    }
}
