package com.supermarket.backend.entity;

import lombok.Data;

@Data
public class OrderItem {
    private Long id;
    private Long orderId;
    private Long productId;
    private Long skuId;
    private String skuCode;
    private String productName;
    private String specification;
    private String unit;
    private Double price;
    private Integer quantity;
    private Double amount;
}
