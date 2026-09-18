package com.supermarket.backend.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@ToString
@EqualsAndHashCode
@TableName("product")
public class Product implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 商品ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 商品名称
     */
    private String name;

    /**
     * 商品条码
     */
    private String barcode;

    /**
     * 商品分类ID
     */
    @TableField("category_id")
    private Long categoryId;

    /**
     * 商品分类名称
     */
    @TableField(exist = false)
    private String categoryName;

    /**
     * 条码命中 SKU 时返回（用于前端下单）
     */
    @TableField(exist = false)
    private Long skuId;

    /**
     * 条码命中 SKU 时返回
     */
    @TableField(exist = false)
    private String skuCode;

    /**
     * 商品规格
     */
    private String specification;

    /**
     * 商品单位
     */
    private String unit;

    /**
     * 商品进价
     */
    @TableField("purchase_price")
    private BigDecimal purchasePrice;

    /**
     * 商品售价
     */
    @TableField("selling_price")
    private BigDecimal sellingPrice;

    /**
     * 商品库存
     */
    private Integer quantity;

    /**
     * 商品最低库存
     */
    @TableField("min_stock")
    private Integer minStock;

    /**
     * 商品描述
     */
    private String description;

    /**
     * 商品状态（0：禁用，1：启用）
     */
    private Integer status;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;
}
