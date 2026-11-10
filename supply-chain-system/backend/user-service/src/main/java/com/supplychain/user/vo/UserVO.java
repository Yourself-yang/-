package com.supplychain.user.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户视图
 */
@Data
public class UserVO {
    private Long id;
    private String username;
    private String realName;
    private String phone;
    private String email;
    private Integer status;
    private String roleCode;
    private String roleName;
    private LocalDateTime createTime;
}
