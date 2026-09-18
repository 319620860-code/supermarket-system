package com.supermarket.backend.controller;

import com.supermarket.backend.entity.Order;
import com.supermarket.backend.entity.OrderItem;
import com.supermarket.backend.service.AuditLogService;
import com.supermarket.backend.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_CASHIER')")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private AuditLogService auditLogService;

    /**
     * 根据ID获取订单
     * @param id 订单ID
     * @return 订单
     */
    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        Order order = orderService.getById(id);
        if (order == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(order);
    }

    /**
     * 根据订单ID获取订单明细行
     */
    @GetMapping("/{id}/items")
    public ResponseEntity<List<OrderItem>> getOrderItems(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderItemsByOrderId(id));
    }

    /**
     * 根据订单号获取订单
     * @param orderNumber 订单号
     * @return 订单
     */
    @GetMapping("/order-number/{orderNumber}")
    public ResponseEntity<Order> getOrderByOrderNumber(@PathVariable String orderNumber) {
        Order order = orderService.getByOrderNumber(orderNumber);
        if (order == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(order);
    }

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
    @GetMapping
    public ResponseEntity<Map<String, Object>> getOrderPage(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String cashierId,
            @RequestParam(required = false) String orderStatus,
            @RequestParam(required = false) String paymentStatus,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            // 处理前端可能传递的额外参数
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) Boolean sortDesc,
            @RequestParam(required = false) String customerName) {
        // 目前只处理前端需要的基本参数，忽略额外参数
        Map<String, Object> pageData = orderService.getPage(keyword, customerName, cashierId, orderStatus, paymentStatus, startDate, endDate, page, pageSize);
        return ResponseEntity.ok(pageData);
    }

    /**
     * 创建订单
     * @param request 订单请求，包含订单信息和订单详情
     * @return 创建后的订单
     */
    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody Map<String, Object> request) {
        Order order = new Order();
        order.setCustomerName((String) request.get("customerName"));
        order.setCustomerPhone((String) request.get("customerPhone"));
        order.setCashierId((String) request.get("cashierId"));
        order.setCashierName((String) request.get("cashierName"));
        
        // 安全地转换折扣金额
        Object discountAmountObj = request.getOrDefault("discountAmount", 0.0);
        Double discountAmount = discountAmountObj instanceof Number ? ((Number) discountAmountObj).doubleValue() : 0.0;
        order.setDiscountAmount(discountAmount);
        
        // 安全地转换支付方式
        Object paymentMethodObj = request.get("paymentMethod");
        String paymentMethod = paymentMethodObj != null ? paymentMethodObj.toString() : "";
        order.setPaymentMethod(paymentMethod);
        
        order.setRemark((String) request.get("remark"));

        // 安全地转换订单项列表（兼容前端字段名 orderItems / items）
        Object orderItemsObj = request.get("orderItems");
        if (orderItemsObj == null) {
            orderItemsObj = request.get("items");
        }
        List<Map<String, Object>> orderItems = new ArrayList<>();
        if (orderItemsObj instanceof List<?>) {
            for (Object item : (List<?>) orderItemsObj) {
                if (item instanceof Map<?, ?>) {
                    Map<String, Object> orderItem = new HashMap<>();
                    for (Map.Entry<?, ?> entry : ((Map<?, ?>) item).entrySet()) {
                        if (entry.getKey() instanceof String) {
                            orderItem.put((String) entry.getKey(), entry.getValue());
                        }
                    }
                    orderItems.add(orderItem);
                }
            }
        }
        
        Order createdOrder = orderService.create(order, orderItems);
        return ResponseEntity.ok(createdOrder);
    }

    /**
     * 更新订单
     * @param id 订单ID
     * @param order 订单
     * @return 更新后的订单
     */
    @PutMapping("/{id}")
    public ResponseEntity<Order> updateOrder(@PathVariable Long id, @RequestBody Order order) {
        order.setId(id);
        Order updatedOrder = orderService.update(order);
        return ResponseEntity.ok(updatedOrder);
    }

    /**
     * 删除订单
     * @param id 订单ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deleteOrder(@PathVariable Long id) {
        boolean deleted = orderService.delete(id);
        return ResponseEntity.ok(deleted);
    }

    /**
     * 获取今日销售额
     * @return 今日销售额
     */
    @GetMapping("/today-sales")
    public ResponseEntity<Double> getTodaySales() {
        Double todaySales = orderService.getTodaySales();
        return ResponseEntity.ok(todaySales);
    }

    /**
     * 获取指定日期范围的销售额
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 指定日期范围的销售额
     */
    @GetMapping("/sales-by-date")
    public ResponseEntity<Double> getSalesByDateRange(@RequestParam String startDate, @RequestParam String endDate) {
        Double sales = orderService.getSalesByDateRange(startDate, endDate);
        return ResponseEntity.ok(sales);
    }

    /**
     * 获取收银员的订单列表
     * @param cashierId 收银员ID
     * @return 订单列表
     */
    @GetMapping("/cashier/{cashierId}")
    public ResponseEntity<List<Order>> getOrdersByCashierId(@PathVariable String cashierId) {
        List<Order> orders = orderService.getByCashierId(cashierId);
        return ResponseEntity.ok(orders);
    }

    /**
     * 支付订单
     * @param id 订单ID
     * @param paymentRequest 支付请求参数（可选）
     * @return 支付后的订单
     */
    @PostMapping("/{id}/pay")
    public ResponseEntity<Order> payOrder(@PathVariable Long id, @RequestBody Map<String, Object> paymentRequest) {
        Order order = orderService.payOrder(id, paymentRequest);
        auditLogService.log("ORDER_PAY", "orderId=" + id + ",orderNumber=" + order.getOrderNumber());
        return ResponseEntity.ok(order);
    }

    /**
     * 取消订单
     * @param id 订单ID
     * @return 取消后的订单
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<Order> cancelOrder(@PathVariable Long id) {
        Order order = orderService.cancelOrder(id);
        auditLogService.log("ORDER_CANCEL", "orderId=" + id + ",orderNumber=" + order.getOrderNumber());
        return ResponseEntity.ok(order);
    }

    /**
     * 退款订单
     * @param id 订单ID
     * @return 退款后的订单
     */
    @PostMapping("/{id}/refund")
    public ResponseEntity<Order> refundOrder(@PathVariable Long id) {
        Order order = orderService.refundOrder(id);
        auditLogService.log("ORDER_REFUND", "orderId=" + id + ",orderNumber=" + order.getOrderNumber());
        return ResponseEntity.ok(order);
    }
}
