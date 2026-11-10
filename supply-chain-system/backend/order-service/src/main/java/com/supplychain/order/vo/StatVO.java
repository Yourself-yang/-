package com.supplychain.order.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class StatVO {
    private Long totalOrders;

    private Long pendingShip;

    private Long shipped;

    private Long completed;

    private Long todayOrders;

    private BigDecimal todaySales;
}
