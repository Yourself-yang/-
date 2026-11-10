package com.supplychain.purchase.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.supplychain.purchase.entity.PurchaseOrderItem;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PurchaseOrderItemMapper extends BaseMapper<PurchaseOrderItem> {
}
