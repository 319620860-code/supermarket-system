package com.supermarket.backend.service;

import com.supermarket.backend.entity.Inventory;

import java.util.List;
import java.util.Map;

public interface InventoryService {
    /**
     * 根据库存ID查询库存
     * @param id 库存ID
     * @return 库存信息
     */
    Inventory getById(Long id);

    /**
     * 分页查询库存列表
     * @param keyword 商品名称关键词
     * @param productId 商品ID
     * @param warehouseId 仓库ID
     * @param status 库存行启用状态（0/1）
     * @param stockLevel 库存水平：normal / low / shortage（与商品最低库存比较）
     * @param page 页码
     * @param pageSize 每页数量
     * @return 库存列表和总数
     */
    Map<String, Object> getPage(String keyword, Long productId, Long warehouseId, Integer status, String stockLevel, Integer page, Integer pageSize);

    /**
     * 根据商品ID和仓库ID查询库存
     * @param productId 商品ID
     * @param warehouseId 仓库ID
     * @return 库存信息
     */
    Inventory getByProductIdAndWarehouseId(Long productId, Long warehouseId);

    /**
     * 查询某商品在所有仓库的库存行
     */
    List<Inventory> listByProductId(Long productId);

    /**
     * 创建库存
     * @param inventory 库存信息
     * @return 创建后的库存信息
     */
    Inventory create(Inventory inventory);

    /**
     * 更新库存信息
     * @param inventory 库存信息
     * @return 更新后的库存信息
     */
    Inventory update(Inventory inventory);

    /**
     * 更新库存数量
     * @param id 库存ID
     * @param quantity 库存数量
     */
    void updateQuantity(Long id, Integer quantity);

    /**
     * 根据商品ID和仓库ID更新库存数量
     * @param productId 商品ID
     * @param warehouseId 仓库ID
     * @param quantity 库存数量
     */
    void updateQuantity(Long productId, Long warehouseId, Integer quantity);

    /**
     * 删除库存
     * @param id 库存ID
     */
    void delete(Long id);

    /**
     * 盘点确认后：若该商品仅有一条分仓库存记录，则把该记录数量与商品表一致（避免 product 与 inventory 脱节）。
     * 多仓并存时不做自动分配，仅更新商品总库存。
     */
    void syncSingleWarehouseInventoryIfPresent(Long productId, int totalQuantity);

    /**
     * 锁定库存（创建订单时调用）
     * @param productId 商品ID
     * @param warehouseId 仓库ID
     * @param quantity 锁定数量
     */
    void lockStock(Long productId, Long warehouseId, Integer quantity);

    /**
     * 解锁库存（取消订单时调用）
     * @param productId 商品ID
     * @param warehouseId 仓库ID
     * @param quantity 解锁数量
     */
    void unlockStock(Long productId, Long warehouseId, Integer quantity);

    /**
     * 扣减锁定库存（支付成功时调用）
     * @param productId 商品ID
     * @param warehouseId 仓库ID
     * @param quantity 扣减数量
     */
    void deductLockedStock(Long productId, Long warehouseId, Integer quantity);
}