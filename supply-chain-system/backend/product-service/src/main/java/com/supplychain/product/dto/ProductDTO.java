package com.supplychain.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductDTO {

    @NotBlank(message = "商品编码不能为空")
    private String productCode;

    @NotBlank(message = "商品名称不能为空")
    private String productName;

    @NotNull(message = "分类ID不能为空")
    private Long categoryId;

    private String brand;

    private String spec;

    @NotBlank(message = "单位不能为空")
    private String unit;

    @NotNull(message = "采购价不能为空")
    private BigDecimal purchasePrice;

    @NotNull(message = "销售价不能为空")
    private BigDecimal salePrice;

    private String imageUrl;
}
