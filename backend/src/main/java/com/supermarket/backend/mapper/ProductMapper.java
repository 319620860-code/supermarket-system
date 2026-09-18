package com.supermarket.backend.mapper;

import com.supermarket.backend.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface ProductMapper {
    
    /**
     * 根据商品ID查询商品
     * @param id 商品ID
     * @return 商品信息
     */
    Product selectById(@Param("id") Long id);
    
    /**
     * 根据商品条码查询商品
     * @param barcode 商品条码
     * @return 商品信息
     */
    Product selectByBarcode(@Param("barcode") String barcode);
    
    /**
     * 分页查询商品列表
     * @param keyword 搜索关键词
     * @param categoryId 分类ID
     * @param status 商品状态
     * @param offset 偏移量
     * @param limit 每页数量
     * @return 商品列表
     */
    List<Product> selectPage(@Param("keyword") String keyword, 
                             @Param("categoryId") Long categoryId, 
                             @Param("status") Integer status,
                             @Param("forSale") Boolean forSale,
                             @Param("offset") Integer offset, 
                             @Param("limit") Integer limit);
    
    /**
     * 查询商品总数
     * @param keyword 搜索关键词
     * @param categoryId 分类ID
     * @param status 商品状态
     * @return 商品总数
     */
    int selectCount(@Param("keyword") String keyword, 
                   @Param("categoryId") Long categoryId, 
                   @Param("status") Integer status,
                   @Param("forSale") Boolean forSale);
    
    /**
     * 插入商品
     * @param product 商品信息
     * @return 影响行数
     */
    int insert(Product product);
    
    /**
     * 更新商品信息
     * @param product 商品信息
     * @return 影响行数
     */
    int update(Product product);
    
    /**
     * 更新商品库存
     * @param id 商品ID
     * @param quantity 库存数量
     * @return 影响行数
     */
    int updateStock(@Param("id") Long id, @Param("quantity") Integer quantity);

    /**
     * 用 SKU 汇总数量回写商品表 quantity（用于列表展示）
     */
    int syncQuantityFromSkus(@Param("productId") Long productId);
    
    /**
     * 删除商品
     * @param id 商品ID
     * @return 影响行数
     */
    int delete(@Param("id") Long id);

    /**
     * 查询库存报表
     * @param params 查询参数
     * @return 库存报表数据
     */
    List<Map<String, Object>> selectInventoryReport(Map<String, Object> params);

    /**
     * 查询低库存商品
     * @param params 查询参数
     * @return 低库存商品列表
     */
    List<Map<String, Object>> selectLowInventory(Map<String, Object> params);
}
