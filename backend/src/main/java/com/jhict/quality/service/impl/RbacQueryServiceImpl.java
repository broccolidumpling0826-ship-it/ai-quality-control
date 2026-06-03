package com.jhict.quality.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jhict.quality.common.util.AuthUtils;
import com.jhict.quality.entity.SysPermission;
import com.jhict.quality.entity.SysRole;
import com.jhict.quality.entity.SysRolePermission;
import com.jhict.quality.entity.SysUser;
import com.jhict.quality.entity.SysUserRole;
import com.jhict.quality.mapper.SysPermissionMapper;
import com.jhict.quality.mapper.SysRoleMapper;
import com.jhict.quality.mapper.SysRolePermissionMapper;
import com.jhict.quality.mapper.SysUserMapper;
import com.jhict.quality.mapper.SysUserRoleMapper;
import com.jhict.quality.service.api.RbacQueryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RbacQueryServiceImpl implements RbacQueryService {

    @Resource
    private SysUserMapper sysUserMapper;

    @Resource
    private SysUserRoleMapper sysUserRoleMapper;

    @Resource
    private SysRoleMapper sysRoleMapper;

    @Resource
    private SysRolePermissionMapper sysRolePermissionMapper;

    @Resource
    private SysPermissionMapper sysPermissionMapper;

    @Override
    public List<String> getUserRoleCodes(String userNo) {
        List<String> roleIds = getUserRoleIds(userNo);
        if (CollectionUtils.isEmpty(roleIds)) {
            return Collections.emptyList();
        }
        return sysRoleMapper.selectBatchIds(roleIds).stream()
                .filter(r -> r.getStatus() != null && r.getStatus() == 1)
                .map(SysRole::getRoleCode)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getUserRoleIds(String userNo) {
        SysUser user = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUserNo, userNo)
                        .eq(SysUser::getStatus, 1)
        );
        if (user == null) {
            return Collections.emptyList();
        }

        List<SysUserRole> userRoles = sysUserRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, user.getId())
        );
        if (!CollectionUtils.isEmpty(userRoles)) {
            return userRoles.stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
        }

        if (StringUtils.hasText(user.getRole())) {
            SysRole role = sysRoleMapper.selectOne(
                    new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleCode, user.getRole())
            );
            if (role != null) {
                return Collections.singletonList(role.getId());
            }
        }
        return Collections.emptyList();
    }

    @Override
    public List<String> getUserPermissionCodes(String userNo) {
        List<String> roleCodes = getUserRoleCodes(userNo);
        if (roleCodes.contains(AuthUtils.ROLE_ADMIN)) {
            return Collections.singletonList("*");
        }

        List<String> roleIds = getUserRoleIds(userNo);
        if (CollectionUtils.isEmpty(roleIds)) {
            return Collections.emptyList();
        }

        List<SysRolePermission> rolePerms = sysRolePermissionMapper.selectList(
                new LambdaQueryWrapper<SysRolePermission>().in(SysRolePermission::getRoleId, roleIds)
        );
        if (CollectionUtils.isEmpty(rolePerms)) {
            return Collections.emptyList();
        }

        Set<String> permIds = rolePerms.stream()
                .map(SysRolePermission::getPermissionId)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        return sysPermissionMapper.selectBatchIds(new ArrayList<>(permIds)).stream()
                .filter(p -> p.getStatus() != null && p.getStatus() == 1)
                .map(SysPermission::getPermCode)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncUserRoles(String userId, List<String> roleCodes, String primaryRoleCode) {
        sysUserRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));

        if (CollectionUtils.isEmpty(roleCodes)) {
            return;
        }

        Set<String> uniqueCodes = new LinkedHashSet<>(roleCodes);
        for (String roleCode : uniqueCodes) {
            SysRole role = sysRoleMapper.selectOne(
                    new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleCode, roleCode)
            );
            if (role == null) {
                continue;
            }
            SysUserRole ur = new SysUserRole();
            ur.setUserId(userId);
            ur.setRoleId(role.getId());
            sysUserRoleMapper.insert(ur);
        }

        SysUser user = sysUserMapper.selectById(userId);
        if (user != null && StringUtils.hasText(primaryRoleCode)) {
            user.setRole(primaryRoleCode);
            sysUserMapper.updateById(user);
        }
    }
}
