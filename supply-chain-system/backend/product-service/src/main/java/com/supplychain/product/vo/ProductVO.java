package com.supplychain.product.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProductVO {

    private Long id;

    private String productCode;

    private String productName;

    private Long categoryId;

    private String categoryName;

    private String brand;

    private String spec;

    private String unit;

    private BigDecimal purchasePrice;

    private BigDecimal salePrice;

    private String imageUrl;

    private Integer status;

    private LocalDateTime createTime;
}
