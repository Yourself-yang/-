package com.supplychain.order.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ShipDTO {
    @NotBlank(message = "物流单号不能为空")
    @JsonAlias({ "trackingNo" })
    private String logisticsNo;
}
