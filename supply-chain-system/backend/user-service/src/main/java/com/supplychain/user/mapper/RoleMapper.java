package com.supplychain.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.supplychain.user.entity.Role;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色Mapper
 */
@Mapper
public interface RoleMapper extends BaseMapper<Role> {
}
