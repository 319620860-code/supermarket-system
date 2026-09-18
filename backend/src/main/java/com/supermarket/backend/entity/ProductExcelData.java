package com.supermarket.backend.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class ProductExcelData {

    @ExcelProperty("商品条码")
    private String barcode;

    @ExcelProperty("商品名称")
    private String name;

    @ExcelProperty("分类ID")
    private Long categoryId;

    @ExcelProperty("规格")
    private String specification;

    @ExcelProperty("单位")
    private String unit;

    @ExcelProperty("进价")
    private Double purchasePrice;

    @ExcelProperty("售价")
    private Double sellingPrice;

    @ExcelProperty("库存数量")
    private Integer quantity;

    @ExcelProperty("预警阈值")
    private Integer alertThreshold;

    @ExcelProperty("状态(1启用/0禁用)")
    private Integer status;
}
