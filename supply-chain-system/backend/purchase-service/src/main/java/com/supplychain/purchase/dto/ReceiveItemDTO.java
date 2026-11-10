package com.supplychain.purchase.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReceiveItemDTO {
    @NotNull(message = "明细ID不能为空")
    private Long itemId;
    @NotNull(message = "入库数量不能为空")
    @Min(value = 1, message = "入库数量至少为1")
    private Integer receivedQty;
}
