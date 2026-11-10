package com.supplychain.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.supplychain.user.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户Mapper
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
