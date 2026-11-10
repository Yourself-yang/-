package com.supplychain.user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.supplychain.user.dto.UserCreateDTO;
import com.supplychain.user.dto.UserUpdateDTO;
import com.supplychain.user.entity.User;
import com.supplychain.user.vo.UserVO;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 分页查询用户
     */
    IPage<UserVO> page(Page<User> page, String keyword, String roleCode, Integer status);

    /**
     * 创建用户
     */
    void create(UserCreateDTO dto);

    /**
     * 更新用户
     */
    void update(Long id, UserUpdateDTO dto);

    /**
     * 删除用户（逻辑删除）
     */
    void delete(Long id);

    /**
     * 分配角色
     */
    void assignRole(Long userId, String roleCode);

    /**
     * 根据ID获取用户
     */
    User getById(Long id);
}
