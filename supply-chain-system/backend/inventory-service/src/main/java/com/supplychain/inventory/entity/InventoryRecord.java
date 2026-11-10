package com.supplychain.inventory.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("inventory_record")
public class InventoryRecord implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("product_id")
    private Long productId;

    private String type;

    @TableField("biz_type")
    private String bizType;

    private Integer quantity;

    @TableField("before_quantity")
    private Integer beforeQuantity;

    @TableField("after_quantity")
    private Integer afterQuantity;

    @TableField("ref_order_no")
    private String refOrderNo;

    private String remark;

    @TableField("operator_id")
    private Long operatorId;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
