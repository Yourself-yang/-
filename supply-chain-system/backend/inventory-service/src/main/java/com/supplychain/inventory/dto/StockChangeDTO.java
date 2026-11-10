package com.supplychain.inventory.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StockChangeDTO {
    @NotNull(message = "商品ID不能为空")
    private Long productId;

    @NotBlank(message = "变动类型不能为空")
    private String type;

    @NotBlank(message = "业务类型不能为空")
    @JsonAlias({ "businessType" })
    private String bizType;

    @NotNull(message = "数量不能为空")
    @Min(value = 1, message = "数量必须大于0")
    private Integer quantity;

    @JsonAlias({ "relatedNo" })
    private String refOrderNo;

    private String remark;
}
