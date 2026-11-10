package com.supplychain.purchase.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.supplychain.common.result.PageResult;
import com.supplychain.common.result.Result;
import com.supplychain.purchase.dto.ApproveDTO;
import com.supplychain.purchase.dto.PurchaseCreateDTO;
import com.supplychain.purchase.dto.ReceiveDTO;
import com.supplychain.purchase.entity.PurchaseOrder;
import com.supplychain.purchase.service.PurchaseService;
import com.supplychain.purchase.vo.PurchaseOrderVO;
import com.supplychain.purchase.vo.PurchaseStatVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({ "/api/v1/purchase", "/api/v1/purchase-orders" })
@RequiredArgsConstructor
public class PurchaseController {

    private final PurchaseService purchaseService;

    @GetMapping("/page")
    public Result<PageResult<PurchaseOrderVO>> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        IPage<PurchaseOrderVO> p = purchaseService.page(keyword, supplierId, status, pageNum, pageSize);
        return Result.success(PageResult.of(p.getTotal(), p.getRecords()));
    }

    @GetMapping
    public Result<PageResult<PurchaseOrderVO>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return page(keyword, supplierId, status, pageNum, pageSize);
    }

    @GetMapping("/{id:\\d+}")
    public Result<PurchaseOrderVO> getById(@PathVariable Long id) {
        return Result.success(purchaseService.getById(id));
    }

    @PostMapping
    public Result<PurchaseOrder> create(@Valid @RequestBody PurchaseCreateDTO dto) {
        return Result.success(purchaseService.create(dto, 1L));
    }

    @PutMapping("/{id:\\d+}/approve")
    public Result<Void> approve(@PathVariable Long id, @RequestBody(required = false) ApproveDTO dto) {
        ApproveDTO body = dto;
        if (body == null) {
            body = new ApproveDTO();
            body.setApproved(true);
        }
        purchaseService.approve(id, body, 1L);
        return Result.success();
    }

    @PutMapping("/{id:\\d+}/ship")
    public Result<Void> ship(@PathVariable Long id) {
        purchaseService.ship(id);
        return Result.success();
    }

    @PutMapping("/{id:\\d+}/receive")
    public Result<Void> receive(@PathVariable Long id, @RequestBody(required = false) ReceiveDTO dto) {
        purchaseService.receive(id, dto != null ? dto : new ReceiveDTO());
        return Result.success();
    }

    @DeleteMapping("/{id:\\d+}")
    public Result<Void> delete(@PathVariable Long id) {
        purchaseService.delete(id);
        return Result.success();
    }

    @GetMapping("/stat")
    public Result<PurchaseStatVO> stat() {
        return Result.success(purchaseService.stat());
    }

    @GetMapping("/stat/overview")
    public Result<PurchaseStatVO> statOverview() {
        return stat();
    }
}
