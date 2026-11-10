package com.supplychain.user.service;

import com.supplychain.user.entity.Role;
import com.supplychain.user.vo.RoleVO;

import java.util.List;

/**
 * 角色服务接口
 */
public interface RoleService {

    /**
     * 查询所有角色列表（带权限）
     */
    List<RoleVO> list();

    /**
     * 根据用户ID获取角色编码列表
     */
    List<String> getRolesByUserId(Long userId);

    /**
     * 根据角色编码获取权限编码列表
     */
    List<String> getPermissionsByRoleCode(String roleCode);

    /**
     * 根据角色编码查询角色ID
     */
    Long getRoleIdByCode(String roleCode);

    /**
     * 根据用户ID获取角色编码（单个）
     */
    String getRoleCodeByUserId(Long userId);

    /**
     * 根据用户ID获取角色信息
     */
    Role getRoleByUserId(Long userId);
}
