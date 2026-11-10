package com.supplychain.purchase.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.supplychain.purchase.dto.ApproveDTO;
import com.supplychain.purchase.dto.PurchaseCreateDTO;
import com.supplychain.purchase.dto.ReceiveDTO;
import com.supplychain.purchase.entity.PurchaseOrder;
import com.supplychain.purchase.vo.PurchaseOrderVO;
import com.supplychain.purchase.vo.PurchaseStatVO;

public interface PurchaseService {
    IPage<PurchaseOrderVO> page(String keyword, Long supplierId, String status, Integer pageNum, Integer pageSize);

    PurchaseOrderVO getById(Long id);

    PurchaseOrder create(PurchaseCreateDTO dto, Long creatorId);

    void approve(Long id, ApproveDTO dto, Long operatorId);

    void ship(Long id);

    void receive(Long id, ReceiveDTO dto);

    void delete(Long id);

    PurchaseStatVO stat();
}
