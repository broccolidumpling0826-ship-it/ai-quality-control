package com.jhict.quality.service.impl;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jhict.quality.common.entity.ApiResult;
import com.jhict.quality.common.exception.ServiceException;
import com.jhict.quality.dto.SysUserCreateCmd;
import com.jhict.quality.dto.SysUserPageQuery;
import com.jhict.quality.dto.SysUserUpdateCmd;
import com.jhict.quality.entity.SysUser;
import com.jhict.quality.mapper.SysUserMapper;
import com.jhict.quality.service.api.UserManageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;

@Service
public class UserManageServiceImpl implements UserManageService {

    private static final String INITIAL_PASSWORD = "Abc@1234";
    private static final int STATUS_ENABLED = 1;

    @Resource
    private SysUserMapper sysUserMapper;

    @Override
    public IPage<SysUser> pageUsers(SysUserPageQuery query) {
        Page<SysUser> page = buildPage(query);
        LambdaQueryWrapper<SysUser> wrapper = buildUserQueryWrapper(query);
        IPage<SysUser> pageResult = sysUserMapper.selectPage(page, wrapper);
        stripPasswords(pageResult);
        return pageResult;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createUser(SysUserCreateCmd cmd) {
        assertUserNoUnique(cmd.getUserNo(), null);
        SysUser user = buildNewUser(cmd);
        sysUserMapper.insert(user);
        return user.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(String id, SysUserUpdateCmd cmd) {
        requireExistingUser(id);
        LambdaUpdateWrapper<SysUser> wrapper = new LambdaUpdateWrapper<SysUser>()
                .eq(SysUser::getId, id);
        applyUserProfileUpdate(wrapper, cmd);
        sysUserMapper.update(null, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserStatus(String id, Integer status) {
        requireExistingUser(id);
        if (status == null) {
            throw new ServiceException(ApiResult.CODE_BAD_REQUEST, "状态不能为空");
        }
        LambdaUpdateWrapper<SysUser> wrapper = new LambdaUpdateWrapper<SysUser>()
                .eq(SysUser::getId, id)
                .set(SysUser::getStatus, status);
        sysUserMapper.update(null, wrapper);
    }

    private Page<SysUser> buildPage(SysUserPageQuery query) {
        int pageNum = query.getPageNum() != null && query.getPageNum() > 0 ? query.getPageNum() : 1;
        int pageSize = query.getPageSize() != null && query.getPageSize() > 0 ? query.getPageSize() : 10;
        return new Page<>(pageNum, pageSize);
    }

    private LambdaQueryWrapper<SysUser> buildUserQueryWrapper(SysUserPageQuery query) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>()
                .orderByAsc(SysUser::getUserNo);
        applyUserNoFilter(wrapper, query.getUserNo());
        applyUsernameFilter(wrapper, query.getUsername());
        applyRoleFilter(wrapper, query.getRole());
        applyStatusFilter(wrapper, query.getStatus());
        return wrapper;
    }

    private void applyUserNoFilter(LambdaQueryWrapper<SysUser> wrapper, String userNo) {
        if (StringUtils.hasText(userNo)) {
            wrapper.like(SysUser::getUserNo, userNo);
        }
    }

    private void applyUsernameFilter(LambdaQueryWrapper<SysUser> wrapper, String username) {
        if (StringUtils.hasText(username)) {
            wrapper.like(SysUser::getUsername, username);
        }
    }

    private void applyRoleFilter(LambdaQueryWrapper<SysUser> wrapper, String role) {
        if (StringUtils.hasText(role)) {
            wrapper.eq(SysUser::getRole, role);
        }
    }

    private void applyStatusFilter(LambdaQueryWrapper<SysUser> wrapper, Integer status) {
        if (status != null) {
            wrapper.eq(SysUser::getStatus, status);
        }
    }

    private void stripPasswords(IPage<SysUser> pageResult) {
        pageResult.getRecords().forEach(user -> user.setPassword(null));
    }

    private SysUser buildNewUser(SysUserCreateCmd cmd) {
        SysUser user = new SysUser();
        user.setUserNo(cmd.getUserNo());
        user.setUsername(cmd.getUsername());
        user.setRole(cmd.getRole());
        user.setDepartment(cmd.getDepartment());
        user.setStatus(STATUS_ENABLED);
        user.setPassword(BCrypt.hashpw(INITIAL_PASSWORD, BCrypt.gensalt()));
        return user;
    }

    private void applyUserProfileUpdate(LambdaUpdateWrapper<SysUser> wrapper, SysUserUpdateCmd cmd) {
        if (cmd.getUsername() != null) {
            wrapper.set(SysUser::getUsername, cmd.getUsername());
        }
        if (cmd.getRole() != null) {
            wrapper.set(SysUser::getRole, cmd.getRole());
        }
        if (cmd.getDepartment() != null) {
            wrapper.set(SysUser::getDepartment, cmd.getDepartment());
        }
    }

    private void assertUserNoUnique(String userNo, String excludeId) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUserNo, userNo);
        if (StringUtils.hasText(excludeId)) {
            wrapper.ne(SysUser::getId, excludeId);
        }
        if (sysUserMapper.selectCount(wrapper) > 0) {
            throw new ServiceException(ApiResult.CODE_BAD_REQUEST, "工号已存在，不可重复");
        }
    }

    private SysUser requireExistingUser(String id) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            throw new ServiceException(ApiResult.CODE_NOT_FOUND, "用户不存在，id=" + id);
        }
        return user;
    }
}
