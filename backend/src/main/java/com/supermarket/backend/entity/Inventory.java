package com.supermarket.backend.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 库存实体类
 */
@Data
public class Inventory {
    /**
     * 库存ID
     */
    private Long id;
    
    /**
     * 商品ID
     */
    private Long productId;
    
    /**
     * 仓库ID
     */
    private Long warehouseId;
    
    /**
     * 库存数量
     */
    private Integer quantity;
    
    /**
     * 锁定数量
     */
    private Integer lockedQuantity;
    
    /**
     * 可用数量
     */
    private Integer availableQuantity;
    
    /**
     * 单位成本
     */
    private BigDecimal unitCost;
    
    /**
     * 状态（1-正常，0-禁用）
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    
    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    /** 关联展示：商品名称（非 inventory 表字段） */
    private String productName;

    /** 关联展示：仓库名称（非 inventory 表字段） */
    private String warehouseName;
}
