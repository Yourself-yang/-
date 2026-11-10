package com.supplychain.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.supplychain.product.entity.Category;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
}
