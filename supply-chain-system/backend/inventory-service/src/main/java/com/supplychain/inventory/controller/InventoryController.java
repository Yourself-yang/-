package com.supplychain.inventory.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.supplychain.common.result.PageResult;
import com.supplychain.common.result.Result;
import com.supplychain.inventory.dto.*;
import com.supplychain.inventory.entity.Inventory;
import com.supplychain.inventory.service.InventoryRecordService;
import com.supplychain.inventory.service.InventoryService;
import com.supplychain.inventory.vo.AlertVO;
import com.supplychain.inventory.vo.InventoryRecordVO;
import com.supplychain.inventory.vo.InventoryVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;
    private final InventoryRecordService inventoryRecordService;

    @GetMapping("/page")
    public Result<PageResult<InventoryVO>> page(InventoryQueryDTO queryDTO) {
        IPage<InventoryVO> p = inventoryService.page(queryDTO);
        return Result.success(PageResult.of(p.getTotal(), p.getRecords()));
    }

    @GetMapping
    public Result<PageResult<InventoryVO>> list(InventoryQueryDTO queryDTO) {
        return page(queryDTO);
    }

    @PostMapping("/stock")
    public Result<Integer> changeStock(@Valid @RequestBody StockChangeDTO dto,
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "1") Long operatorId) {
        return Result.success(inventoryService.changeStock(dto, operatorId));
    }

    @PostMapping("/change")
    public Result<Integer> changeStockAlias(@Valid @RequestBody StockChangeDTO dto,
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "1") Long operatorId) {
        return changeStock(dto, operatorId);
    }

    @PostMapping("/transfer")
    public Result<Void> transfer(@Valid @RequestBody StockTransferDTO dto,
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "1") Long operatorId) {
        inventoryService.transfer(dto, operatorId);
        return Result.success();
    }

    @PostMapping("/check")
    public Result<Void> check(@Valid @RequestBody StockCheckDTO dto,
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "1") Long operatorId) {
        inventoryService.check(dto, operatorId);
        return Result.success();
    }

    @GetMapping("/records")
    public Result<PageResult<InventoryRecordVO>> records(
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) String bizType,
            @RequestParam(required = false) String businessType,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        String bt = bizType != null ? bizType : businessType;
        IPage<InventoryRecordVO> p = inventoryRecordService.page(productId, bt, pageNum, pageSize);
        return Result.success(PageResult.of(p.getTotal(), p.getRecords()));
    }

    @GetMapping("/alerts")
    public Result<List<AlertVO>> alerts() {
        return Result.success(inventoryService.alerts());
    }

    @GetMapping("/product/{pid:\\d+}")
    public Result<Inventory> getByProduct(@PathVariable Long pid) {
        return Result.success(inventoryService.getByProductId(pid));
    }

    @GetMapping("/{productId:\\d+}")
    public Result<Inventory> getByProductId(@PathVariable Long productId) {
        return Result.success(inventoryService.getByProductId(productId));
    }
}
