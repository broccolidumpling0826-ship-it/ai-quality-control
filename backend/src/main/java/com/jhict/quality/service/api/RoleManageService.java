package com.jhict.quality.service.api;

import com.jhict.quality.dto.RoleSaveCmd;
import com.jhict.quality.vo.PermissionVO;
import com.jhict.quality.vo.RoleVO;

import java.util.List;

public interface RoleManageService {

    List<RoleVO> listRoles();

    RoleVO createRole(RoleSaveCmd cmd);

    RoleVO updateRole(String id, RoleSaveCmd cmd);

    void deleteRole(String id);

    List<String> getRoleMenuIds(String roleId);

    void assignMenus(String roleId, List<String> menuIds);

    List<String> getRolePermissionCodes(String roleId);

    void assignPermissions(String roleId, List<String> permCodes);

    List<PermissionVO> listPermissions();
}
