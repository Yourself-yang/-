package com.supplychain.purchase.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PurchaseItemVO {
    private Long id;
    private Long orderId;
    private Long productId;
    private String productName;
    private Integer quantity;
    private Integer receivedQuantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
}
