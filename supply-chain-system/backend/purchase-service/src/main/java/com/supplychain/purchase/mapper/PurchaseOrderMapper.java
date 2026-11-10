package com.supplychain.purchase.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.supplychain.purchase.entity.PurchaseOrder;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PurchaseOrderMapper extends BaseMapper<PurchaseOrder> {
}
