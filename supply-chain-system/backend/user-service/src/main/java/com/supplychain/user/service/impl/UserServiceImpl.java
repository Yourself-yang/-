package com.supplychain.user.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.supplychain.common.exception.BusinessException;
import com.supplychain.common.util.PasswordUtil;
import com.supplychain.user.dto.UserCreateDTO;
import com.supplychain.user.dto.UserUpdateDTO;
import com.supplychain.user.entity.Role;
import com.supplychain.user.entity.User;
import com.supplychain.user.entity.UserRole;
import com.supplychain.user.mapper.UserMapper;
import com.supplychain.user.mapper.UserRoleMapper;
import com.supplychain.user.service.RoleService;
import com.supplychain.user.service.UserService;
import com.supplychain.user.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户服务实现
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final RoleService roleService;

    @Override
    public IPage<UserVO> page(Page<User> page, String keyword, String roleCode, Integer status) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(User::getUsername, keyword).or().like(User::getRealName, keyword));
        }
        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }
        wrapper.orderByDesc(User::getCreateTime);

        IPage<User> userPage = userMapper.selectPage(page, wrapper);

        List<UserVO> voList = new ArrayList<>();
        for (User user : userPage.getRecords()) {
            UserVO vo = convertToVO(user);
            if (StrUtil.isNotBlank(roleCode) && !roleCode.equals(vo.getRoleCode())) {
                continue;
            }
            voList.add(vo);
        }

        IPage<UserVO> voPage = new Page<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(UserCreateDTO dto) {
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername())
        );
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(PasswordUtil.encode(dto.getPassword()));
        user.setRealName(dto.getRealName());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setStatus(1);
        userMapper.insert(user);

        if (StrUtil.isNotBlank(dto.getRoleCode())) {
            Long roleId = roleService.getRoleIdByCode(dto.getRoleCode());
            UserRole ur = new UserRole();
            ur.setUserId(user.getId());
            ur.setRoleId(roleId);
            userRoleMapper.insert(ur);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, UserUpdateDTO dto) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (StrUtil.isNotBlank(dto.getRealName())) {
            user.setRealName(dto.getRealName());
        }
        if (dto.getPhone() != null) {
            user.setPhone(dto.getPhone());
        }
        if (dto.getEmail() != null) {
            user.setEmail(dto.getEmail());
        }
        if (dto.getStatus() != null) {
            user.setStatus(dto.getStatus());
        }
        userMapper.updateById(user);

        if (StrUtil.isNotBlank(dto.getRoleCode())) {
            userRoleMapper.delete(new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, id));
            Long roleId = roleService.getRoleIdByCode(dto.getRoleCode());
            UserRole ur = new UserRole();
            ur.setUserId(id);
            ur.setRoleId(roleId);
            userRoleMapper.insert(ur);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        userMapper.deleteById(id);
        userRoleMapper.delete(new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignRole(Long userId, String roleCode) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        Long roleId = roleService.getRoleIdByCode(roleCode);
        userRoleMapper.delete(new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, userId));
        UserRole ur = new UserRole();
        ur.setUserId(userId);
        ur.setRoleId(roleId);
        userRoleMapper.insert(ur);
    }

    @Override
    public User getById(Long id) {
        return userMapper.selectById(id);
    }

    /**
     * User转UserVO
     */
    private UserVO convertToVO(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setPhone(user.getPhone());
        vo.setEmail(user.getEmail());
        vo.setStatus(user.getStatus());
        vo.setCreateTime(user.getCreateTime());

        Role role = roleService.getRoleByUserId(user.getId());
        if (role != null) {
            vo.setRoleCode(role.getRoleCode());
            vo.setRoleName(role.getRoleName());
        }
        return vo;
    }
}
