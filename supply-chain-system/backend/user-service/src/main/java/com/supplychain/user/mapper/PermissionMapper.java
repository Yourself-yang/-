package com.supplychain.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.supplychain.user.entity.Permission;
import org.apache.ibatis.annotations.Mapper;

/**
 * 权限Mapper
 */
@Mapper
public interface PermissionMapper extends BaseMapper<Permission> {
}
