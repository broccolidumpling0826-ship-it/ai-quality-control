package com.jhict.quality.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jhict.quality.common.entity.ApiResult;
import com.jhict.quality.common.exception.ServiceException;
import com.jhict.quality.dto.RoleSaveCmd;
import com.jhict.quality.entity.SysPermission;
import com.jhict.quality.entity.SysRole;
import com.jhict.quality.entity.SysRoleMenu;
import com.jhict.quality.entity.SysRolePermission;
import com.jhict.quality.mapper.SysPermissionMapper;
import com.jhict.quality.mapper.SysRoleMapper;
import com.jhict.quality.mapper.SysRoleMenuMapper;
import com.jhict.quality.mapper.SysRolePermissionMapper;
import com.jhict.quality.service.api.MenuService;
import com.jhict.quality.service.api.RoleManageService;
import com.jhict.quality.vo.PermissionVO;
import com.jhict.quality.vo.RoleVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoleManageServiceImpl implements RoleManageService {

    @Resource
    private SysRoleMapper sysRoleMapper;

    @Resource
    private SysRoleMenuMapper sysRoleMenuMapper;

    @Resource
    private SysRolePermissionMapper sysRolePermissionMapper;

    @Resource
    private SysPermissionMapper sysPermissionMapper;

    @Resource
    private MenuService menuService;

    @Override
    public List<RoleVO> listRoles() {
        return sysRoleMapper.selectList(
                new LambdaQueryWrapper<SysRole>().orderByAsc(SysRole::getSortOrder)
        ).stream().map(this::toRoleVo).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RoleVO createRole(RoleSaveCmd cmd) {
        Long exists = sysRoleMapper.selectCount(
                new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleCode, cmd.getRoleCode())
        );
        if (exists != null && exists > 0) {
            throw new ServiceException(ApiResult.CODE_BAD_REQUEST, "角色编码已存在");
        }
        SysRole role = new SysRole();
        BeanUtils.copyProperties(cmd, role);
        sysRoleMapper.insert(role);
        menuService.evictAllMenuCache();
        return toRoleVo(role);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RoleVO updateRole(String id, RoleSaveCmd cmd) {
        SysRole role = sysRoleMapper.selectById(id);
        if (role == null) {
            throw new ServiceException(ApiResult.CODE_BAD_REQUEST, "角色不存在");
        }
        BeanUtils.copyProperties(cmd, role);
        role.setId(id);
        sysRoleMapper.updateById(role);
        menuService.evictAllMenuCache();
        return toRoleVo(role);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRole(String id) {
        sysRoleMapper.deleteById(id);
        sysRoleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, id));
        sysRolePermissionMapper.delete(new LambdaQueryWrapper<SysRolePermission>().eq(SysRolePermission::getRoleId, id));
        menuService.evictAllMenuCache();
    }

    @Override
    public List<String> getRoleMenuIds(String roleId) {
        return sysRoleMenuMapper.selectList(
                new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, roleId)
        ).stream().map(SysRoleMenu::getMenuId).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignMenus(String roleId, List<String> menuIds) {
        sysRoleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, roleId));
        if (CollectionUtils.isEmpty(menuIds)) {
            menuService.evictAllMenuCache();
            return;
        }
        for (String menuId : menuIds) {
            SysRoleMenu rm = new SysRoleMenu();
            rm.setRoleId(roleId);
            rm.setMenuId(menuId);
            sysRoleMenuMapper.insert(rm);
        }
        menuService.evictAllMenuCache();
    }

    @Override
    public List<String> getRolePermissionCodes(String roleId) {
        List<SysRolePermission> rps = sysRolePermissionMapper.selectList(
                new LambdaQueryWrapper<SysRolePermission>().eq(SysRolePermission::getRoleId, roleId)
        );
        if (CollectionUtils.isEmpty(rps)) {
            return Collections.emptyList();
        }
        List<String> permIds = rps.stream().map(SysRolePermission::getPermissionId).collect(Collectors.toList());
        return sysPermissionMapper.selectBatchIds(permIds).stream()
                .map(SysPermission::getPermCode)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignPermissions(String roleId, List<String> permCodes) {
        sysRolePermissionMapper.delete(
                new LambdaQueryWrapper<SysRolePermission>().eq(SysRolePermission::getRoleId, roleId)
        );
        if (CollectionUtils.isEmpty(permCodes)) {
            menuService.evictAllMenuCache();
            return;
        }
        for (String permCode : permCodes) {
            SysPermission perm = sysPermissionMapper.selectOne(
                    new LambdaQueryWrapper<SysPermission>().eq(SysPermission::getPermCode, permCode)
            );
            if (perm == null) {
                continue;
            }
            SysRolePermission rp = new SysRolePermission();
            rp.setRoleId(roleId);
            rp.setPermissionId(perm.getId());
            sysRolePermissionMapper.insert(rp);
        }
        menuService.evictAllMenuCache();
    }

    @Override
    public List<PermissionVO> listPermissions() {
        return sysPermissionMapper.selectList(
                new LambdaQueryWrapper<SysPermission>().orderByAsc(SysPermission::getPermCode)
        ).stream().map(p -> PermissionVO.builder()
                .id(p.getId())
                .permCode(p.getPermCode())
                .permName(p.getPermName())
                .permType(p.getPermType())
                .description(p.getDescription())
                .status(p.getStatus())
                .build()).collect(Collectors.toList());
    }

    private RoleVO toRoleVo(SysRole role) {
        return RoleVO.builder()
                .id(role.getId())
                .roleCode(role.getRoleCode())
                .roleName(role.getRoleName())
                .description(role.getDescription())
                .sortOrder(role.getSortOrder())
                .status(role.getStatus())
                .build();
    }
}
