package com.supermarket.backend.entity;

import lombok.Data;
import java.util.Date;

@Data
public class StockOutRecord {
    private Long id;
    private String batchNumber;
    private Long productId;
    private Long warehouseId;
    private Long orderId;
    private Integer quantity;
    private Double unitPrice;
    private Double totalPrice;
    private String outType;
    private String outTarget;
    private Long targetWarehouseId;
    private Long operatorId;
    private String remark;
    private Integer status;
    private Date createTime;
    private Date updateTime;
}
