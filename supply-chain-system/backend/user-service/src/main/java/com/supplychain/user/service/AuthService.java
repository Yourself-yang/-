package com.supplychain.user.service;

import com.supplychain.user.dto.LoginDTO;
import com.supplychain.user.dto.PasswordDTO;
import com.supplychain.user.vo.LoginVO;
import com.supplychain.user.vo.UserVO;

/**
 * 认证服务接口
 */
public interface AuthService {

    /**
     * 登录
     */
    LoginVO login(LoginDTO dto);

    /**
     * 登出
     */
    void logout(String token);

    /**
     * 获取当前用户信息
     */
    UserVO getCurrentUser(Long userId);

    /**
     * 修改密码
     */
    void changePassword(Long userId, PasswordDTO dto);
}
