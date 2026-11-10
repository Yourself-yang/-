package com.supplychain.purchase.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PurchaseStatVO {
    private Long pendingCount;
    private Long approvedCount;
    private Long shippedCount;
    private Long receivedCount;
    private BigDecimal totalAmount;
}
