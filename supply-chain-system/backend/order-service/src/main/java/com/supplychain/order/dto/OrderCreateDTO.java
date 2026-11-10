package com.supplychain.order.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class OrderCreateDTO {
    @NotBlank(message = "收货人姓名不能为空")
    @JsonAlias({ "receiver" })
    private String receiverName;

    @NotBlank(message = "收货人电话不能为空")
    @JsonAlias({ "phone" })
    private String receiverPhone;

    @NotBlank(message = "收货地址不能为空")
    @JsonAlias({ "address" })
    private String receiverAddress;

    @Valid
    @NotEmpty(message = "订单商品不能为空")
    private List<OrderItemDTO> items;
}
