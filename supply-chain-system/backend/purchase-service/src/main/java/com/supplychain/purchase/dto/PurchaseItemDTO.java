package com.supplychain.purchase.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PurchaseItemDTO {
    @NotNull(message = "商品ID不能为空")
    private Long productId;
    @NotNull(message = "数量不能为空")
    @Min(value = 1, message = "数量至少为1")
    private Integer quantity;
    @NotNull(message = "单价不能为空")
    @DecimalMin(value = "0.01", message = "单价至少为0.01")
    private BigDecimal unitPrice;
}
