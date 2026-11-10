package com.supplychain.order.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderVO {
    private Long id;

    private String orderNo;

    private BigDecimal totalAmount;

    private String status;

    private String receiverName;

    @JsonProperty("receiver")
    public String getReceiver() {
        return receiverName;
    }

    private String receiverPhone;

    @JsonProperty("phone")
    public String getPhone() {
        return receiverPhone;
    }

    private String receiverAddress;

    @JsonProperty("address")
    public String getAddress() {
        return receiverAddress;
    }

    private String logisticsNo;

    @JsonProperty("trackingNo")
    public String getTrackingNo() {
        return logisticsNo;
    }

    private LocalDateTime payTime;

    private LocalDateTime createTime;

    private LocalDateTime shipTime;

    private LocalDateTime finishTime;

    private List<OrderItemVO> items;
}
