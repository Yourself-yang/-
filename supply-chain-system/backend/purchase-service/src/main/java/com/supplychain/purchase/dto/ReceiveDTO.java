package com.supplychain.purchase.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class ReceiveDTO {
    @Valid
    @NotEmpty(message = "入库明细不能为空")
    private List<ReceiveItemDTO> items;
}
