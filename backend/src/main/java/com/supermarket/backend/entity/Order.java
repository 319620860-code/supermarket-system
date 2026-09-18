package com.supermarket.backend.entity;

import lombok.Data;
import java.util.Date;

@Data
public class Order {
    private Long id;
    private String orderNumber;
    private String customerName;
    private String customerPhone;
    private String cashierId;
    private String cashierName;
    private Long operatorId;
    private Double totalAmount;
    private Double discountAmount;
    private Double actualAmount;
    private String paymentMethod;
    private String paymentStatus;
    private String orderStatus;
    private String remark;
    private Date createTime;
    private Date updateTime;
}
