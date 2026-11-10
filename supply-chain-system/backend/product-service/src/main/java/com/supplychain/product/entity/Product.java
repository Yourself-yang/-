package com.supplychain.product.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.supplychain.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("product")
public class Product extends BaseEntity {

    private String productCode;

    private String productName;

    private Long categoryId;

    private String brand;

    private String spec;

    private String unit;

    private BigDecimal purchasePrice;

    private BigDecimal salePrice;

    private String imageUrl;

    @TableField("`status`")
    private Integer status;
}
