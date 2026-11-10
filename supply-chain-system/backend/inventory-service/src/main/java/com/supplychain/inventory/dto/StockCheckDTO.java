package com.supplychain.inventory.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StockCheckDTO {
    @NotNull(message = "商品ID不能为空")
    private Long productId;

    @NotNull(message = "实盘数量不能为空")
    @Min(value = 0, message = "实盘数量不能为负数")
    @JsonAlias({ "actualStock" })
    private Integer actualQuantity;

    private String remark;
}
