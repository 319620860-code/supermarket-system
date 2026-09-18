package com.supermarket.backend.entity;

import lombok.Data;
import java.util.Date;

@Data
public class StockCheckRecord {
    private Long id;
    private String checkNumber;
    private Long productId;
    private Integer actualQuantity;
    private Integer systemQuantity;
    private Integer difference;
    private Double unitPrice;
    private Double differenceAmount;
    private Long operatorId;
    private String remark;
    private Integer status;
    private Date createTime;
    private Date updateTime;
}
