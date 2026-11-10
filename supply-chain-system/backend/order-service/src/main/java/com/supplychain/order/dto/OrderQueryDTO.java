package com.supplychain.order.dto;

import lombok.Data;

@Data
public class OrderQueryDTO {
    private String keyword;

    private String status;

    private String startTime;

    private String endTime;

    private Integer pageNum = 1;

    private Integer pageSize = 10;
}
