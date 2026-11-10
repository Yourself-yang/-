package com.supplychain.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StockTransferDTO {
    @NotNull(message = "商品ID不能为空")
    private Long productId;

    @NotNull(message = "调拨数量不能为空")
    @Min(value = 1, message = "调拨数量必须大于0")
    private Integer quantity;

    @NotBlank(message = "源仓库不能为空")
    private String fromWarehouse;

    @NotBlank(message = "目标仓库不能为空")
    private String toWarehouse;

    private String remark;
}
