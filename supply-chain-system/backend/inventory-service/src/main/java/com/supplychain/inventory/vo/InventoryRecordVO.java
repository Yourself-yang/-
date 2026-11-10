package com.supplychain.inventory.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InventoryRecordVO {
    private Long id;

    private Long productId;

    private String type;

    private String bizType;

    @JsonProperty("businessType")
    public String getBusinessType() {
        return bizType;
    }

    private Integer quantity;

    private Integer beforeQuantity;

    @JsonProperty("beforeStock")
    public Integer getBeforeStock() {
        return beforeQuantity;
    }

    private Integer afterQuantity;

    @JsonProperty("afterStock")
    public Integer getAfterStock() {
        return afterQuantity;
    }

    private String refOrderNo;

    @JsonProperty("relatedNo")
    public String getRelatedNo() {
        return refOrderNo;
    }

    private String remark;

    private Long operatorId;

    private String operatorName;

    @JsonProperty("operator")
    public String getOperator() {
        return operatorName != null ? operatorName : String.valueOf(operatorId);
    }

    private LocalDateTime createTime;

    private String productName;
}
