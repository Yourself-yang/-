package com.supplychain.purchase.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ApproveDTO {
    @NotNull(message = "审批结果不能为空")
    private Boolean approved;
    private String remark;
}
