package com.supplychain.user.dto;

import lombok.Data;

/**
 * 更新用户请求
 */
@Data
public class UserUpdateDTO {
    private String realName;
    private String phone;
    private String email;
    private Integer status;
    private String roleCode;
}
