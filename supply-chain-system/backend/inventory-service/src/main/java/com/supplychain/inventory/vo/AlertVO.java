package com.supplychain.inventory.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AlertVO {
    private Long productId;

    private String productCode;

    private String productName;

    private Integer quantity;

    @JsonProperty("currentStock")
    public Integer getCurrentStock() {
        return quantity;
    }

    private Integer safetyStock;

    private String warehouse;

    private String message;
}
