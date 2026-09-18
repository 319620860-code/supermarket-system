package com.supermarket.backend.entity;

import lombok.Data;
import java.util.Date;

@Data
public class StockInRecord {
    private Long id;
    private String batchNumber;
    private Long productId;
    /** 入库仓库（与库存表一致） */
    private Long warehouseId;
    /** 供应商（写入 stock_in_record.supplier_id） */
    private Long supplierId;
    /** 操作人 sys_user.id（服务端根据登录态写入） */
    private Long operatorId;
    private Integer quantity;
    private Double unitPrice;
    private Double totalPrice;
    private String remark;
    private Integer status;
    private Date createTime;
    private Date updateTime;
}