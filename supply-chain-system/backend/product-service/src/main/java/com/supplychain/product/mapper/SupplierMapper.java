package com.supplychain.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.supplychain.product.entity.Supplier;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SupplierMapper extends BaseMapper<Supplier> {
}
