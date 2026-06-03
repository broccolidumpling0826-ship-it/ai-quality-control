package com.jhict.quality.service.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jhict.quality.dto.SysUserCreateCmd;
import com.jhict.quality.dto.SysUserPageQuery;
import com.jhict.quality.dto.SysUserUpdateCmd;
import com.jhict.quality.entity.SysUser;

public interface UserManageService {

    /**
     * 分页查询用户（响应中不含密码）
     */
    IPage<SysUser> pageUsers(SysUserPageQuery query);

    /**
     * 新增用户
     *
     * @return 新用户ID
     */
    String createUser(SysUserCreateCmd cmd);

    /**
     * 更新用户信息（仅 username/role/department）
     */
    void updateUser(String id, SysUserUpdateCmd cmd);

    /**
     * 启用/禁用用户
     */
    void updateUserStatus(String id, Integer status);

    /**
     * 分配用户角色（多选）
     */
    void assignUserRoles(String id, java.util.List<String> roleCodes);
}
