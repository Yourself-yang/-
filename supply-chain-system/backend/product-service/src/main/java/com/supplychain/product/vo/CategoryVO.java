package com.supplychain.product.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CategoryVO {

    private Long id;

    private String categoryName;

    private Long parentId;

    private Integer sort;

    private LocalDateTime createTime;
}
