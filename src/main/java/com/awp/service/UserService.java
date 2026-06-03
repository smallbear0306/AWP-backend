package com.awp.service;

import com.awp.dto.LoginDTO;
import com.awp.dto.LoginVO;
import com.awp.dto.RegisterDTO;
import com.awp.entity.User;

/**
 * 用户业务接口。
 */
public interface UserService {

    /** 注册 */
    void register(RegisterDTO dto);

    /** 登录，返回 token 与基本信息 */
    LoginVO login(LoginDTO dto);

    /** 获取当前登录用户信息 */
    User getCurrentUser();
}
