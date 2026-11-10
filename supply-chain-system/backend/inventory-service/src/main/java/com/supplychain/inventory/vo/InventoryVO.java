package com.supplychain.inventory.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InventoryVO {
    private Long id;

    private Long productId;

    private Integer quantity;

    @JsonProperty("stock")
    public Integer getStock() {
        return quantity;
    }

    private Integer safetyStock;

    private String warehouse;

    private LocalDateTime updateTime;

    private String productName;

    private String productCode;
}
