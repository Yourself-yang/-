package com.supplychain.inventory.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.supplychain.inventory.dto.*;
import com.supplychain.inventory.entity.Inventory;
import com.supplychain.inventory.vo.AlertVO;
import com.supplychain.inventory.vo.InventoryVO;

import java.util.List;

public interface InventoryService {
    IPage<InventoryVO> page(InventoryQueryDTO queryDTO);

    Inventory getByProductId(Long productId);

    Integer changeStock(StockChangeDTO dto, Long operatorId);

    void transfer(StockTransferDTO dto, Long operatorId);

    void check(StockCheckDTO dto, Long operatorId);

    List<AlertVO> alerts();
}
