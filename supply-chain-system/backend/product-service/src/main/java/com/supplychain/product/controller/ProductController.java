package com.supplychain.product.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.supplychain.common.result.PageResult;
import com.supplychain.common.result.Result;
import com.supplychain.product.dto.ProductDTO;
import com.supplychain.product.dto.ProductQueryDTO;
import com.supplychain.product.service.ProductService;
import com.supplychain.product.vo.ProductVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping("/page")
    public Result<PageResult<ProductVO>> page(ProductQueryDTO queryDTO) {
        IPage<ProductVO> p = productService.page(queryDTO);
        return Result.success(PageResult.of(p.getTotal(), p.getRecords()));
    }

    @GetMapping
    public Result<PageResult<ProductVO>> list(ProductQueryDTO queryDTO) {
        return page(queryDTO);
    }

    @GetMapping("/{id:\\d+}")
    public Result<ProductVO> getById(@PathVariable Long id) {
        return Result.success(productService.getById(id));
    }

    @PostMapping
    public Result<Void> create(@Valid @RequestBody ProductDTO dto) {
        productService.create(dto);
        return Result.success();
    }

    @PutMapping("/{id:\\d+}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody ProductDTO dto) {
        productService.update(id, dto);
        return Result.success();
    }

    @PutMapping("/{id:\\d+}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody StatusBody body) {
        productService.updateStatus(id, body.getStatus());
        return Result.success();
    }

    @DeleteMapping("/{id:\\d+}")
    public Result<Void> delete(@PathVariable Long id) {
        productService.delete(id);
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
