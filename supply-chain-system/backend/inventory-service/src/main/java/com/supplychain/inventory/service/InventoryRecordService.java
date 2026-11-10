package com.supplychain.inventory.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.supplychain.inventory.vo.InventoryRecordVO;

public interface InventoryRecordService {
    IPage<InventoryRecordVO> page(Long productId, String bizType, Integer pageNum, Integer pageSize);
}
