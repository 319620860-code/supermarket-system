package com.supermarket.backend.service;

import com.supermarket.backend.entity.Order;
import com.supermarket.backend.entity.OrderItem;
import java.util.List;
import java.util.Map;

public interface OrderService {
    /**
     * 根据ID获取订单
     * @param id 订单ID
     * @return 订单
     */
    Order getById(Long id);

    /**
     * 根据订单号获取订单
     * @param orderNumber 订单号
     * @return 订单
     */
    Order getByOrderNumber(String orderNumber);

    /**
     * 分页获取订单列表
     * @param keyword 搜索关键词
     * @param cashierId 收银员ID
     * @param orderStatus 订单状态
     * @param paymentStatus 支付状态
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param page 页码
     * @param pageSize 每页数量
     * @return 订单列表和总数
     */
    Map<String, Object> getPage(String keyword, String customerName, String cashierId, String orderStatus, String paymentStatus, String startDate, String endDate, Integer page, Integer pageSize);

    /**
     * 根据订单ID查询订单明细
     */
    List<OrderItem> getOrderItemsByOrderId(Long orderId);

    /**
     * 创建订单
     * @param order 订单
     * @param orderItems 订单详情列表
     * @return 创建后的订单
     */
    Order create(Order order, List<Map<String, Object>> orderItems);

    /**
     * 更新订单
     * @param order 订单
     * @return 更新后的订单
     */
    Order update(Order order);

    /**
     * 删除订单
     * @param id 订单ID
     * @return 删除结果
     */
    boolean delete(Long id);

    /**
     * 获取今日销售额
     * @return 今日销售额
     */
    Double getTodaySales();

    /**
     * 获取指定日期范围的销售额
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 指定日期范围的销售额
     */
    Double getSalesByDateRange(String startDate, String endDate);

    /**
     * 指定时间范围内已完成且已支付订单数量
     */
    Integer getOrderCountByDateRange(String startDate, String endDate);

    /**
     * 获取收银员的订单列表
     * @param cashierId 收银员ID
     * @return 订单列表
     */
    List<Order> getByCashierId(String cashierId);

    /**
     * 支付订单
     * @param id 订单ID
     * @return 支付后的订单
     */
    Order payOrder(Long id);
    
    /**
     * 支付订单
     * @param id 订单ID
     * @param paymentRequest 支付请求参数
     * @return 支付后的订单
     */
    Order payOrder(Long id, Map<String, Object> paymentRequest);

    /**
     * 取消订单
     * @param id 订单ID
     * @return 取消后的订单
     */
    Order cancelOrder(Long id);

    /**
     * 退款订单
     * @param id 订单ID
     * @return 退款后的订单
     */
    Order refundOrder(Long id);
}
