package com.supermarket.backend.service;

import com.supermarket.backend.entity.Product;

import java.util.List;
import java.util.Map;

public interface ProductService {
    
    /**
     * 根据商品ID查询商品
     * @param id 商品ID
     * @return 商品信息
     */
    Product getById(Long id);
    
    /**
     * 根据条码查询商品（先匹配 SKU 条码，再可选匹配主档条码）。
     */
    default Product getByBarcode(String barcode) {
        return getByBarcode(barcode, false, false);
    }

    /**
     * @param skuOnly 为 true 时仅匹配 SKU 条码（收银扫码），不回落主档条码
     */
    default Product getByBarcode(String barcode, boolean skuOnly) {
        return getByBarcode(barcode, skuOnly, false);
    }

    /**
     * @param sellableOnly 为 true 时仅返回可售商品：上架且所属分类启用，否则返回 null
     */
    Product getByBarcode(String barcode, boolean skuOnly, boolean sellableOnly);
    
    /**
     * 分页查询商品列表
     * @param keyword 搜索关键词
     * @param categoryId 分类ID
     * @param status 商品状态
     * @param page 页码
     * @param pageSize 每页数量
     * @return 商品列表和总数
     */
    Map<String, Object> getPage(String keyword, Long categoryId, Integer status, Boolean forSale, Integer page, Integer pageSize);
    
    /**
     * 创建商品
     * @param product 商品信息
     * @return 创建后的商品信息
     */
    Product create(Product product);
    
    /**
     * 更新商品信息
     * @param product 商品信息
     * @return 更新后的商品信息
     */
    Product update(Product product);
    
    /**
     * 更新商品库存
     * @param id 商品ID
     * @param quantity 库存数量
     * @return 是否更新成功
     */
    boolean updateStock(Long id, Integer quantity);
    
    /**
     * 删除商品
     * @param id 商品ID
     * @return 是否删除成功
     */
    boolean delete(Long id);
    
    /**
     * 批量删除商品
     * @param ids 商品ID列表
     * @return 删除成功的数量
     */
    int batchDelete(List<Long> ids);
}
