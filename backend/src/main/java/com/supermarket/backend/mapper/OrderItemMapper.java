package com.supermarket.backend.mapper;

import com.supermarket.backend.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface OrderItemMapper {
    /**
     * 根据ID查询订单详情
     * @param id 订单详情ID
     * @return 订单详情
     */
    OrderItem selectById(@Param("id") Long id);

    /**
     * 根据订单ID查询订单详情
     * @param orderId 订单ID
     * @return 订单详情列表
     */
    List<OrderItem> selectByOrderId(@Param("orderId") Long orderId);

    /**
     * 根据订单号查询订单详情
     * @param orderNumber 订单号
     * @return 订单详情列表
     */
    List<OrderItem> selectByOrderNumber(@Param("orderNumber") String orderNumber);

    /**
     * 批量插入订单详情
     * @param orderItems 订单详情列表
     * @return 插入成功的记录数
     */
    int insertBatch(List<OrderItem> orderItems);

    /**
     * 根据订单ID删除订单详情
     * @param orderId 订单ID
     * @return 删除成功的记录数
     */
    int deleteByOrderId(@Param("orderId") Long orderId);

    /**
     * 查询商品销售统计
     * @param params 查询参数
     * @return 商品销售统计列表
     */
    List<Map<String, Object>> selectProductSalesStatistics(Map<String, Object> params);

    /**
     * 查询收银员销售统计
     * @param params 查询参数
     * @return 收银员销售统计列表
     */
    List<Map<String, Object>> selectCashierSalesStatistics(Map<String, Object> params);

    /**
     * 查询每日销售统计
     * @param params 查询参数
     * @return 每日销售统计列表
     */
    List<Map<String, Object>> selectDailySalesStatistics(Map<String, Object> params);
    
    /**
     * 查询销售利润统计
     * @param params 查询参数
     * @return 销售利润统计列表
     */
    List<Map<String, Object>> selectProductSalesProfit(Map<String, Object> params);
}
