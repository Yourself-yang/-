package com.supplychain.purchase.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PurchaseOrderVO {
    private Long id;

    private String orderNo;

    @JsonProperty("poNo")
    public String getPoNo() {
        return orderNo;
    }

    private Long supplierId;
    private String supplierName;
    private BigDecimal totalAmount;
    private String status;
    private String remark;
    private Long creatorId;
    private LocalDateTime createTime;
    private LocalDateTime receiveTime;
    private List<PurchaseItemVO> items;
}
