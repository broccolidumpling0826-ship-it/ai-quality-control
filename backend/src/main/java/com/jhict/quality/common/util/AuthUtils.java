package com.jhict.quality.common.util;

import cn.dev33.satoken.stp.StpUtil;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 认证/角色工具。系统管理员（ADMIN）拥有全部业务角色权限，业务层与注解校验均应通过本类判断。
 */
public final class AuthUtils {

    public static final String ROLE_ADMIN = "ADMIN";

    private static final List<String> ALL_BUSINESS_ROLES = Collections.unmodifiableList(Arrays.asList(
            ROLE_ADMIN,
            "QUALITY_ENGINEER",
            "QUALITY_SUPERVISOR",
            "QUALITY_MANAGER",
            "SALES_MANAGER"
    ));

    private AuthUtils() {
    }

    public static boolean isAdmin() {
        return ROLE_ADMIN.equals(currentRole());
    }

    /** 当前登录用户角色（来自 Session） */
    public static String currentRole() {
        try {
            Object role = StpUtil.getSession().get("role");
            if (role != null && StringUtils.hasText(role.toString())) {
                return role.toString();
            }
        } catch (Exception ignored) {
            // 未登录
        }
        return "";
    }

    /**
     * 是否拥有指定角色。ADMIN 恒为 true（跳过角色校验）。
     */
    public static boolean hasRole(String role) {
        if (!StringUtils.hasText(role)) {
            return false;
        }
        if (isAdmin()) {
            return true;
        }
        try {
            return StpUtil.hasRole(role);
        } catch (Exception e) {
            return false;
        }
    }

    /** ADMIN 登录时返回全部角色，供 {@code @SaCheckRole} 通过任意角色校验 */
    public static List<String> allRolesForAdmin() {
        return ALL_BUSINESS_ROLES;
    }
}
