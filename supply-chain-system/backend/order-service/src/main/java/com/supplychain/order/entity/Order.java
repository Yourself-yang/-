package com.supplychain.order.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("orders")
public class Order {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;

    private BigDecimal totalAmount;

    private String status;

    private String receiverName;

    private String receiverPhone;

    private String receiverAddress;

    private String logisticsNo;

    private LocalDateTime payTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    private LocalDateTime shipTime;

    private LocalDateTime finishTime;

    @TableLogic
    private Integer deleted;
}
