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

    /**
     * 超时未支付订单自动取消（幂等）。
     *
     * <p>由 RocketMQ 延迟消息消费者调用：校验订单是否确实已超过支付超时时间，
     * 且仍处于「未支付 + CREATED」，才执行取消并解锁库存；
     * 已支付/已取消的订单直接忽略，重复投递不会产生副作用。
     *
     * @param orderId 订单ID
     * @return 本次调用是否真正执行了取消
     */
    boolean autoCancelIfTimeout(Long orderId);

    /**
     * 查询已超时未支付的订单（兜底扫描用）。
     *
     * <p>仅做查询，不做状态变更；调用方应逐条调用
     * {@link #autoCancelIfTimeout(Long)} 以经由事务代理单个处理。
     *
     * @param limit 单批最大条数
     * @return 超时未支付订单列表
     */
    List<Order> findTimeoutUnpaidOrders(int limit);
}
