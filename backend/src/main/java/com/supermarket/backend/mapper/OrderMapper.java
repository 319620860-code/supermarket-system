package com.supermarket.backend.mapper;

import com.supermarket.backend.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {
    /**
     * 根据ID查询订单
     * @param id 订单ID
     * @return 订单
     */
    Order selectById(@Param("id") Long id);

    /**
     * 根据订单号查询订单
     * @param orderNumber 订单号
     * @return 订单
     */
    Order selectByOrderNumber(@Param("orderNumber") String orderNumber);

    /**
     * 分页查询订单列表
     * @param params 查询参数
     * @return 订单列表
     */
    List<Order> selectPage(Map<String, Object> params);

    /**
     * 查询订单总数
     * @param params 查询参数
     * @return 订单总数
     */
    Integer selectCount(Map<String, Object> params);

    /**
     * 插入订单
     * @param order 订单
     * @return 插入成功的记录数
     */
    int insert(Order order);

    /**
     * 更新订单
     * @param order 订单
     * @return 更新成功的记录数
     */
    int update(Order order);

    /**
     * 删除订单
     * @param id 订单ID
     * @return 删除成功的记录数
     */
    int delete(@Param("id") Long id);

    /**
     * 根据收银员ID查询订单
     * @param cashierId 收银员ID
     * @return 订单列表
     */
    List<Order> selectByCashierId(@Param("cashierId") String cashierId);

    /**
     * 查询今日销售额
     * @return 今日销售额
     */
    Double selectTodaySales();

    /**
     * 查询指定日期范围的销售额
     * @param params 查询参数
     * @return 销售额
     */
    Double selectSalesByDateRange(Map<String, Object> params);

    /**
     * 查询指定日期范围的订单数量
     * @param params 查询参数
     * @return 订单数量
     */
    Integer selectOrderCountByDateRange(Map<String, Object> params);
}
