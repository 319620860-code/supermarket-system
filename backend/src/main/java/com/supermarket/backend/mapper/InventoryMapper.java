package com.supermarket.backend.mapper;

import com.supermarket.backend.entity.Inventory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface InventoryMapper {

    /**
     * 查询某商品在所有仓库的库存行
     */
    List<Inventory> selectByProductId(@Param("productId") Long productId);
    /**
     * 根据ID查询库存
     * @param id 库存ID
     * @return 库存信息
     */
    Inventory selectById(@Param("id") Long id);

    /**
     * 分页查询库存列表
     * @param params 查询参数
     * @return 库存列表
     */
    List<Inventory> selectPage(Map<String, Object> params);

    /**
     * 查询库存总数
     * @param params 查询参数
     * @return 库存总数
     */
    int selectCount(Map<String, Object> params);

    /**
     * 根据商品ID和仓库ID查询库存
     * @param productId 商品ID
     * @param warehouseId 仓库ID
     * @return 库存信息
     */
    Inventory selectByProductIdAndWarehouseId(@Param("productId") Long productId, @Param("warehouseId") Long warehouseId);

    /**
     * 插入库存
     * @param inventory 库存信息
     * @return 插入成功的记录数
     */
    int insert(Inventory inventory);

    /**
     * 更新库存
     * @param inventory 库存信息
     * @return 更新成功的记录数
     */
    int update(Inventory inventory);

    /**
     * 更新库存数量
     * @param id 库存ID
     * @param quantity 库存数量
     * @return 更新成功的记录数
     */
    int updateQuantity(@Param("id") Long id, @Param("quantity") Integer quantity);

    /**
     * 删除库存
     * @param id 库存ID
     * @return 删除成功的记录数
     */
    int delete(@Param("id") Long id);

    /**
     * 锁定库存（创建订单时调用）
     * @param productId 商品ID
     * @param warehouseId 仓库ID
     * @param quantity 锁定数量
     * @return 更新成功的记录数
     */
    int lockStock(@Param("productId") Long productId, @Param("warehouseId") Long warehouseId, @Param("quantity") Integer quantity);

    /**
     * 解锁库存（取消订单时调用）
     * @param productId 商品ID
     * @param warehouseId 仓库ID
     * @param quantity 解锁数量
     * @return 更新成功的记录数
     */
    int unlockStock(@Param("productId") Long productId, @Param("warehouseId") Long warehouseId, @Param("quantity") Integer quantity);

    /**
     * 扣减锁定库存（支付成功时调用）
     * @param productId 商品ID
     * @param warehouseId 仓库ID
     * @param quantity 扣减数量
     * @return 更新成功的记录数
     */
    int deductLockedStock(@Param("productId") Long productId, @Param("warehouseId") Long warehouseId, @Param("quantity") Integer quantity);
}