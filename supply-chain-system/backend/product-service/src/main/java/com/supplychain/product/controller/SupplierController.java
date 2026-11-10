package com.supplychain.product.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.supplychain.common.result.PageResult;
import com.supplychain.common.result.Result;
import com.supplychain.product.dto.SupplierDTO;
import com.supplychain.product.entity.Supplier;
import com.supplychain.product.service.SupplierService;
import com.supplychain.product.vo.SupplierVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    @GetMapping("/page")
    public Result<PageResult<SupplierVO>> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        IPage<SupplierVO> p = supplierService.page(keyword, pageNum, pageSize);
        return Result.success(PageResult.of(p.getTotal(), p.getRecords()));
    }

    @GetMapping
    public Result<List<SupplierVO>> listAll() {
        return Result.success(supplierService.listAll());
    }

    @GetMapping("/{id:\\d+}")
    public Result<Supplier> getById(@PathVariable Long id) {
        return Result.success(supplierService.getById(id));
    }

    @PostMapping
    public Result<Void> create(@Valid @RequestBody SupplierDTO dto) {
        supplierService.create(dto);
        return Result.success();
    }

    @PutMapping("/{id:\\d+}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody SupplierDTO dto) {
        supplierService.update(id, dto);
        return Result.success();
    }

    @DeleteMapping("/{id:\\d+}")
    public Result<Void> delete(@PathVariable Long id) {
        supplierService.delete(id);
        return Result.success();
    }

    @PutMapping("/{id:\\d+}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody StatusBody body) {
        supplierService.updateStatus(id, body.getStatus());
        return Result.success();
    }

    public static class StatusBody {
        private Integer status;

        public Integer getStatus() {
            return status;
        }

        public void setStatus(Integer status) {
            this.status = status;
        }
    }
}
