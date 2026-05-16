package com.jhict.quality.service.api;

import com.jhict.quality.dto.LoginCmd;
import com.jhict.quality.vo.LoginVO;

public interface AuthService {

    /**
     * 用户登录
     *
     * @param cmd 登录参数（工号 + 密码）
     * @return 登录信息（Token + 用户基本信息）
     */
    LoginVO login(LoginCmd cmd);

    /**
     * 用户退出登录
     */
    void logout();

    /**
     * 获取当前登录用户信息
     *
     * @return 当前用户信息
     */
    LoginVO getUserInfo();
}
