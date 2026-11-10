package com.supplychain.user.vo;

import lombok.Data;

import java.util.List;

/**
 * 角色视图
 */
@Data
public class RoleVO {
    private Long id;
    private String roleCode;
    private String roleName;
    private String description;
    private List<PermissionVO> permissions;
}
