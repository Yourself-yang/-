package com.supplychain.inventory.dto;

import lombok.Data;

@Data
public class InventoryQueryDTO {
    private String keyword;

    private String warehouse;

    private Boolean lowStock;

    private Integer pageNum = 1;

    private Integer pageSize = 10;
}
