package com.supplychain.order.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderItemVO {
    private Long id;

    private Long orderId;

    private Long productId;

    private String productName;

    private String productImage;

    private Integer quantity;

    private BigDecimal unitPrice;

    private BigDecimal subtotal;

    private LocalDateTime createTime;
}
