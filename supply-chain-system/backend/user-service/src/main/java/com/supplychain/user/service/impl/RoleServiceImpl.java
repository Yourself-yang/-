package com.supplychain.user.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.supplychain.common.exception.BusinessException;
import com.supplychain.user.entity.Permission;
import com.supplychain.user.entity.Role;
import com.supplychain.user.entity.RolePermission;
import com.supplychain.user.entity.UserRole;
import com.supplychain.user.mapper.PermissionMapper;
import com.supplychain.user.mapper.RoleMapper;
import com.supplychain.user.mapper.RolePermissionMapper;
import com.supplychain.user.mapper.UserRoleMapper;
import com.supplychain.user.service.RoleService;
import com.supplychain.user.vo.PermissionVO;
import com.supplychain.user.vo.RoleVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色服务实现
 */
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleMapper roleMapper;
    private final PermissionMapper permissionMapper;
    private final UserRoleMapper userRoleMapper;
    private final RolePermissionMapper rolePermissionMapper;

    @Override
    public List<RoleVO> list() {
        List<Role> roles = roleMapper.selectList(null);
        if (CollUtil.isEmpty(roles)) {
            return Collections.emptyList();
        }
        List<RoleVO> voList = new ArrayList<>();
        for (Role role : roles) {
            RoleVO vo = new RoleVO();
            vo.setId(role.getId());
            vo.setRoleCode(role.getRoleCode());
            vo.setRoleName(role.getRoleName());
            vo.setDescription(role.getDescription());
            vo.setPermissions(getPermissionVOsByRoleId(role.getId()));
            voList.add(vo);
        }
        return voList;
    }

    @Override
    public List<String> getRolesByUserId(Long userId) {
        List<UserRole> urList = userRoleMapper.selectList(
                new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, userId)
        );
        if (CollUtil.isEmpty(urList)) {
            return Collections.emptyList();
        }
        List<Long> roleIds = urList.stream().map(UserRole::getRoleId).collect(Collectors.toList());
        List<Role> roles = roleMapper.selectBatchIds(roleIds);
        return roles.stream().map(Role::getRoleCode).collect(Collectors.toList());
    }

    @Override
    public List<String> getPermissionsByRoleCode(String roleCode) {
        Role role = roleMapper.selectOne(
                new LambdaQueryWrapper<Role>().eq(Role::getRoleCode, roleCode)
        );
        if (role == null) {
            return Collections.emptyList();
        }
        List<RolePermission> rpList = rolePermissionMapper.selectList(
                new LambdaQueryWrapper<RolePermission>().eq(RolePermission::getRoleId, role.getId())
        );
        if (CollUtil.isEmpty(rpList)) {
            return Collections.emptyList();
        }
        List<Long> permIds = rpList.stream().map(RolePermission::getPermissionId).collect(Collectors.toList());
        List<Permission> permissions = permissionMapper.selectBatchIds(permIds);
        return permissions.stream().map(Permission::getPermCode).collect(Collectors.toList());
    }

    @Override
    public Long getRoleIdByCode(String roleCode) {
        Role role = roleMapper.selectOne(
                new LambdaQueryWrapper<Role>().eq(Role::getRoleCode, roleCode)
        );
        if (role == null) {
            throw new BusinessException("角色不存在: " + roleCode);
        }
        return role.getId();
    }

    @Override
    public String getRoleCodeByUserId(Long userId) {
        Role role = getRoleByUserId(userId);
        return role != null ? role.getRoleCode() : null;
    }

    @Override
    public Role getRoleByUserId(Long userId) {
        List<UserRole> urList = userRoleMapper.selectList(
                new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, userId)
        );
        if (CollUtil.isEmpty(urList)) {
            return null;
        }
        Long roleId = urList.get(0).getRoleId();
        return roleMapper.selectById(roleId);
    }

    /**
     * 根据角色ID获取权限VO列表
     */
    private List<PermissionVO> getPermissionVOsByRoleId(Long roleId) {
        List<RolePermission> rpList = rolePermissionMapper.selectList(
                new LambdaQueryWrapper<RolePermission>().eq(RolePermission::getRoleId, roleId)
        );
        if (CollUtil.isEmpty(rpList)) {
            return Collections.emptyList();
        }
        List<Long> permIds = rpList.stream().map(RolePermission::getPermissionId).collect(Collectors.toList());
        List<Permission> permissions = permissionMapper.selectBatchIds(permIds);
        return permissions.stream().map(p -> {
            PermissionVO pvo = new PermissionVO();
            pvo.setId(p.getId());
            pvo.setPermCode(p.getPermCode());
            pvo.setPermName(p.getPermName());
            return pvo;
        }).collect(Collectors.toList());
    }
}
