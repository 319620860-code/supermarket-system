package com.supermarket.backend.dto;

import lombok.Data;

import java.util.Date;

/**
 * 入库明细行 + 主单信息（用于列表展示）
 */
@Data
public class StockInItemLineVO {
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
    private Long supplierId;
    private String supplierName;
    /** 操作人 ID */
    private Long operatorId;
    /** 操作人展示名 */
    private String operatorName;
    private String remark;
    private Date createTime;
}
