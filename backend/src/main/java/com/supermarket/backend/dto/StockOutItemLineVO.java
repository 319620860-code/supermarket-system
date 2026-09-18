package com.supermarket.backend.dto;

import lombok.Data;

import java.util.Date;

/**
 * 出库明细行 + 主单信息（用于列表展示）
 */
@Data
public class StockOutItemLineVO {
    private Long id;
    private Long recordId;
    private String recordNo;
    private Long productId;
    private String productName;
    private String specification;
    private String unit;
    private Double unitPrice;
    private Integer quantity;
    private Double lineAmount;
    private String outType;
    private Long operatorId;
    private String operatorName;
    private String remark;
    private Date createTime;
}
