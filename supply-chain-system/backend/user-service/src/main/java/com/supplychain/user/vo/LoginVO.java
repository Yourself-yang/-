package com.supplychain.user.vo;

import lombok.Data;

import java.util.List;

/**
 * 登录响应
 */
@Data
public class LoginVO {
    private String token;
    private Long userId;
    private String username;
    private String realName;
    private String role;
    private List<String> permissions;
}
