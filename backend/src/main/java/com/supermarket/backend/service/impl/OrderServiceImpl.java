package com.supermarket.backend.service.impl;

import com.supermarket.backend.config.OrderTimeoutProperties;
import com.supermarket.backend.mapper.CategoryMapper;
import com.supermarket.backend.mapper.OrderItemMapper;
import com.supermarket.backend.mapper.OrderMapper;
import com.supermarket.backend.mapper.PaymentRecordMapper;
import com.supermarket.backend.mapper.ProductMapper;
import com.supermarket.backend.entity.Category;
import com.supermarket.backend.entity.Order;
import com.supermarket.backend.entity.OrderItem;
import com.supermarket.backend.entity.Product;
import com.supermarket.backend.mq.OrderTimeoutProducer;
import com.supermarket.backend.service.OrderService;
import com.supermarket.backend.service.InventoryService;
import com.supermarket.backend.service.SysUserService;
import com.supermarket.backend.util.PagePayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 订单与库存约定：创建订单仅校验库存可用性并锁定库存；支付成功时扣减库存；
 * 取消未支付订单释放锁定库存；退款时恢复库存；删除订单时按已支付/已退款状态决定是否恢复库存。
 *
 * <p>订单超时自动取消：下单成功后投递 RocketMQ 延迟消息（默认 10 分钟），
 * 到期由消费者触发取消；另有兜底扫描任务保证 MQ 不可用时依然生效。
 */
@Service
public class OrderServiceImpl implements OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private ProductMapper productMapper;

    /** 支付流水 mapper（叶子依赖，不构成循环依赖）：订单取消/超时关闭时同步关闭进行中的支付单 */
    @Autowired
    private PaymentRecordMapper paymentRecordMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private OrderTimeoutProperties timeoutProperties;

    /** MQ 生产者为可选依赖：order.timeout.mq.enabled=false 时不存在，此时仅依赖兜底扫描 */
    @Autowired
    private ObjectProvider<OrderTimeoutProducer> orderTimeoutProducerProvider;

    @Override
    public Order getById(Long id) {
        return orderMapper.selectById(id);
    }

    @Override
    public Order getByOrderNumber(String orderNumber) {
        return orderMapper.selectByOrderNumber(orderNumber);
    }

    @Override
    public List<OrderItem> getOrderItemsByOrderId(Long orderId) {
        if (orderId == null) {
            return Collections.emptyList();
        }
        return orderItemMapper.selectByOrderId(orderId);
    }

    @Override
    public Map<String, Object> getPage(String keyword, String customerName, String cashierId, String orderStatus, String paymentStatus, String startDate, String endDate, Integer page, Integer pageSize) {
        page = page == null ? 1 : page;
        pageSize = pageSize == null ? 10 : pageSize;

        Map<String, Object> params = new HashMap<>();
        params.put("keyword", keyword);
        params.put("customerName", customerName);
        params.put("cashierId", cashierId);
        params.put("orderStatus", orderStatus);
        params.put("paymentStatus", paymentStatus);
        params.put("startDate", startDate);
        params.put("endDate", endDate);
        params.put("start", (page - 1) * pageSize);
        params.put("pageSize", pageSize);

        List<Order> orders = orderMapper.selectPage(params);
        Integer count = orderMapper.selectCount(params);
        int total = count != null ? count : 0;

        return PagePayload.of(orders, total, page, pageSize);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"salesStatistics", "productRanking", "salesTrend", "salesProfit"}, allEntries = true)
    public Order create(Order order, List<Map<String, Object>> orderItems) {
        if (orderItems == null || orderItems.isEmpty()) {
            throw new IllegalArgumentException("订单明细不能为空");
        }

        if (order.getCustomerName() == null || order.getCustomerName().trim().isEmpty()) {
            throw new IllegalArgumentException("客户名称不能为空");
        }

        // 根据cashierId获取用户信息，设置cashierName
        if (order.getCashierId() != null) {
            try {
                Long cashierIdLong = Long.parseLong(order.getCashierId());
                com.supermarket.backend.entity.SysUser user = sysUserService.getById(cashierIdLong);
                if (user != null) {
                    if (user.getRealName() != null && !user.getRealName().isEmpty()) {
                        order.setCashierName(user.getRealName());
                    } else {
                        order.setCashierName(user.getUsername());
                    }
                }
            } catch (Exception e) {
                // 忽略所有错误，确保订单创建流程不被中断
                System.out.println("获取收银员名称失败: " + e.getMessage());
            }
        }

        String orderNumber = generateOrderNumber();
        order.setOrderNumber(orderNumber);
        order.setCreateTime(new Date());
        order.setUpdateTime(new Date());

        double totalAmount = 0.0;
        for (Map<String, Object> item : orderItems) {
            double unitPrice = lineUnitPrice(item);
            int quantity = Integer.parseInt(item.get("quantity").toString());
            totalAmount += unitPrice * quantity;
        }
        order.setTotalAmount(totalAmount);
        order.setActualAmount(totalAmount - (order.getDiscountAmount() != null ? order.getDiscountAmount() : 0));
        order.setPaymentStatus("UNPAID");
        order.setOrderStatus("CREATED");

        // 校验库存并锁定（创建订单时锁定库存）
        for (Map<String, Object> item : orderItems) {
            Long productId = Long.parseLong(item.get("productId").toString());
            assertProductSellable(productId);
            int qty = Integer.parseInt(item.get("quantity").toString());
            
            // 获取商品默认仓库（简化处理，使用仓库ID=1）
            Long warehouseId = 1L;
            
            // 直接校验商品库存
            Product product = productMapper.selectById(productId);
            if (product == null) {
                throw new IllegalArgumentException("商品不存在，ID: " + productId);
            }
            int stock = product.getQuantity() == null ? 0 : product.getQuantity();
            if (stock < qty) {
                throw new IllegalArgumentException("库存不足，商品: " + product.getName() + "，当前库存: " + stock);
            }
            
            // 锁定库存（增加锁定数量，减少可用数量）
            try {
                inventoryService.lockStock(productId, warehouseId, qty);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("库存不足或商品不可用，商品ID: " + productId);
            }
        }

        orderMapper.insert(order);

        List<OrderItem> items = new ArrayList<>();
        for (Map<String, Object> item : orderItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getId());
            orderItem.setProductId(Long.parseLong(item.get("productId").toString()));
            orderItem.setProductName(item.get("productName").toString());
            orderItem.setSpecification(item.getOrDefault("specification", "").toString());
            orderItem.setUnit(item.getOrDefault("unit", "").toString());
            double price = lineUnitPrice(item);
            Integer quantity = Integer.parseInt(item.get("quantity").toString());
            orderItem.setPrice(price);
            orderItem.setQuantity(quantity);
            orderItem.setAmount(price * quantity);
            items.add(orderItem);
        }

        if (!items.isEmpty()) {
            orderItemMapper.insertBatch(items);
        }

        // 事务提交后再投递延迟消息，避免事务回滚导致「订单不存在却收到超时消息」
        scheduleTimeoutCheckAfterCommit(order);

        return order;
    }

    /**
     * 注册「事务提交后」回调投递 RocketMQ 延迟消息；无事务时立即投递。
     */
    private void scheduleTimeoutCheckAfterCommit(Order order) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    sendTimeoutMessage(order);
                }
            });
        } else {
            sendTimeoutMessage(order);
        }
    }

    private void sendTimeoutMessage(Order order) {
        OrderTimeoutProducer producer = orderTimeoutProducerProvider.getIfAvailable();
        if (producer == null) {
            logger.info("RocketMQ 未启用，订单 {} 的超时取消交由兜底扫描处理", order.getOrderNumber());
            return;
        }
        producer.sendOrderTimeoutMessage(order);
    }

    @Override
    public Order update(Order order) {
        order.setUpdateTime(new Date());
        orderMapper.update(order);
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            return false;
        }
        // 已支付且已完成：删除前退回库存（等价于先退款再删）
        if ("PAID".equals(order.getPaymentStatus()) && "COMPLETED".equals(order.getOrderStatus())) {
            restoreStockForOrder(id);
        }
        // 已退款：退款时已恢复库存，仅删数据
        orderItemMapper.deleteByOrderId(id);
        orderMapper.delete(id);
        return true;
    }

    @Override
    public Double getTodaySales() {
        return orderMapper.selectTodaySales() != null ? orderMapper.selectTodaySales() : 0.0;
    }

    @Override
    public Double getSalesByDateRange(String startDate, String endDate) {
        Map<String, Object> params = new HashMap<>();
        params.put("startDate", startDate);
        params.put("endDate", endDate);
        return orderMapper.selectSalesByDateRange(params) != null ? orderMapper.selectSalesByDateRange(params) : 0.0;
    }

    @Override
    public Integer getOrderCountByDateRange(String startDate, String endDate) {
        Map<String, Object> params = new HashMap<>();
        params.put("startDate", startDate);
        params.put("endDate", endDate);
        Integer n = orderMapper.selectOrderCountByDateRange(params);
        return n != null ? n : 0;
    }

    @Override
    public List<Order> getByCashierId(String cashierId) {
        return orderMapper.selectByCashierId(cashierId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Order payOrder(Long id) {
        return payOrder(id, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"salesStatistics", "productRanking", "salesTrend", "salesProfit"}, allEntries = true)
    public Order payOrder(Long id, Map<String, Object> paymentRequest) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new IllegalStateException("订单不存在");
        }
        if (!"UNPAID".equals(order.getPaymentStatus())) {
            throw new IllegalStateException("订单已支付或已退款，请勿重复支付");
        }
        if (!"CREATED".equals(order.getOrderStatus())) {
            throw new IllegalStateException("订单状态不允许支付");
        }

        String paymentMethod = order.getPaymentMethod();
        Double actualAmount = order.getActualAmount();
        String remark = order.getRemark();

        // 处理支付参数
        if (paymentRequest != null) {
            Object paymentMethodObj = paymentRequest.get("paymentMethod");
            if (paymentMethodObj != null) {
                paymentMethod = paymentMethodObj.toString();
            }
            Object amountObj = paymentRequest.get("amount");
            if (amountObj instanceof Number) {
                actualAmount = ((Number) amountObj).doubleValue();
            }
            Object remarkObj = paymentRequest.get("remark");
            if (remarkObj != null) {
                remark = remark != null ? remark + " | " + remarkObj : remarkObj.toString();
            }
        }

        // 幂等「占位」：条件更新（WHERE payment_status='UNPAID' AND order_status='CREATED'），
        // 并发/重复支付时只有一个请求能占到订单，避免库存被重复扣减
        int claimed = orderMapper.payIfUnpaid(id, paymentMethod, actualAmount, remark, new Date());
        if (claimed == 0) {
            throw new IllegalStateException("订单已被支付或已取消，请刷新后重试");
        }

        // 占位成功后扣减库存；此处若抛异常，事务回滚会连同订单占位一起回退
        deductStockForOrder(id);

        return orderMapper.selectById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"salesStatistics", "productRanking", "salesTrend", "salesProfit"}, allEntries = true)
    public Order cancelOrder(Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new IllegalStateException("订单不存在");
        }
        if (!"CREATED".equals(order.getOrderStatus())) {
            throw new IllegalStateException("订单状态不允许取消");
        }
        if (!"UNPAID".equals(order.getPaymentStatus())) {
            throw new IllegalStateException("已支付订单请使用退款流程");
        }

        // 幂等取消：条件更新保证并发下只有一次生效（RocketMQ 延迟消息到达时也会走同一方法）
        int updated = orderMapper.cancelIfUnpaid(id, new Date());
        if (updated == 0) {
            throw new IllegalStateException("订单状态已变更，请刷新后重试");
        }

        // 解锁库存（取消订单时释放此前锁定的库存）
        unlockStockForOrder(id);

        // 关闭该订单下进行中的支付单，避免迟到的支付回调为已取消订单记账
        paymentRecordMapper.closePendingByOrderId(id, new Date());

        return orderMapper.selectById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"salesStatistics", "productRanking", "salesTrend", "salesProfit"}, allEntries = true)
    public Order refundOrder(Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new IllegalStateException("订单不存在");
        }
        if (!"PAID".equals(order.getPaymentStatus())) {
            throw new IllegalStateException("订单未支付，无法退款");
        }
        if (!"COMPLETED".equals(order.getOrderStatus())) {
            throw new IllegalStateException("订单状态不允许退款");
        }

        restoreStockForOrder(id);

        order.setPaymentStatus("REFUNDED");
        order.setOrderStatus("REFUNDED");
        order.setUpdateTime(new Date());
        orderMapper.update(order);

        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"salesStatistics", "productRanking", "salesTrend", "salesProfit"}, allEntries = true)
    public boolean autoCancelIfTimeout(Long orderId) {
        if (orderId == null) {
            return false;
        }
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            logger.warn("收到订单超时消息但订单不存在: orderId={}", orderId);
            return false;
        }
        // 状态已流转（用户已支付，或已被其它途径取消）：幂等忽略，不做任何操作
        if (!"UNPAID".equals(order.getPaymentStatus()) || !"CREATED".equals(order.getOrderStatus())) {
            logger.info("订单无需超时取消（状态已流转）: orderNumber={}, paymentStatus={}, orderStatus={}",
                    order.getOrderNumber(), order.getPaymentStatus(), order.getOrderStatus());
            return false;
        }
        // 二次校验是否真的超时，防止延迟等级配置异常导致消息提前到达
        long timeoutMillis = timeoutMillis();
        if (order.getCreateTime() != null
                && System.currentTimeMillis() - order.getCreateTime().getTime() < timeoutMillis) {
            logger.warn("订单超时消息提前到达，本次忽略: orderNumber={}, createTime={}",
                    order.getOrderNumber(), order.getCreateTime());
            return false;
        }

        // 条件更新占位：与用户支付并发时，只有一方能成功
        int updated = orderMapper.cancelIfUnpaid(orderId, new Date());
        if (updated == 0) {
            logger.info("订单已被并发处理（可能刚完成支付），忽略本次超时取消: orderNumber={}", order.getOrderNumber());
            return false;
        }

        // 占位成功后释放此前锁定的库存
        unlockStockForOrder(orderId);
        // 同步关闭该订单进行中的支付单
        paymentRecordMapper.closePendingByOrderId(orderId, new Date());
        logger.info("订单超时未支付，已自动取消并释放锁定库存: orderNumber={}, 超时阈值={}分钟",
                order.getOrderNumber(), timeoutProperties.getMinutes());
        return true;
    }

    @Override
    public List<Order> findTimeoutUnpaidOrders(int limit) {
        Date deadline = new Date(System.currentTimeMillis() - timeoutMillis());
        List<Order> list = orderMapper.selectTimeoutUnpaid(deadline, limit <= 0 ? 200 : limit);
        return list == null ? Collections.emptyList() : list;
    }

    /** 支付超时毫秒数，下限 1 分钟，避免配置为 0 造成误取消 */
    private long timeoutMillis() {
        return Math.max(1, timeoutProperties.getMinutes()) * 60_000L;
    }

    private void deductStockForOrder(Long orderId) {
        List<OrderItem> items = orderItemMapper.selectByOrderId(orderId);
        Long warehouseId = 1L; // 默认仓库
        
        for (OrderItem line : items) {
            int need = line.getQuantity() == null ? 0 : line.getQuantity();
            Long productId = line.getProductId();
            
            // 扣减锁定库存（减少锁定数量和总库存）
            inventoryService.deductLockedStock(productId, warehouseId, need);
            
            // 直接扣减商品库存
            Product product = productMapper.selectById(productId);
            if (product == null) {
                throw new IllegalStateException("商品不存在，ID: " + productId);
            }
            int stock = product.getQuantity() == null ? 0 : product.getQuantity();
            if (stock < need) {
                throw new IllegalStateException("库存不足，无法完成支付，商品: " + product.getName());
            }
            product.setQuantity(stock - need);
            productMapper.update(product);
        }
    }

    private void unlockStockForOrder(Long orderId) {
        List<OrderItem> items = orderItemMapper.selectByOrderId(orderId);
        Long warehouseId = 1L; // 默认仓库
        
        for (OrderItem line : items) {
            int need = line.getQuantity() == null ? 0 : line.getQuantity();
            Long productId = line.getProductId();
            
            // 解锁库存（减少锁定数量，增加可用数量）
            try {
                inventoryService.unlockStock(productId, warehouseId, need);
            } catch (IllegalArgumentException e) {
                // 如果解锁失败（比如库存已经被释放），记录日志但不抛出异常
                System.out.println("解锁库存失败，可能库存已释放，商品ID: " + productId);
            }
        }
    }

    private void restoreStockForOrder(Long orderId) {
        List<OrderItem> items = orderItemMapper.selectByOrderId(orderId);
        for (OrderItem line : items) {
            int back = line.getQuantity() == null ? 0 : line.getQuantity();
            Product product = productMapper.selectById(line.getProductId());
            if (product == null) {
                continue;
            }
            int stock = product.getQuantity() == null ? 0 : product.getQuantity();
            product.setQuantity(stock + back);
            productMapper.update(product);
        }
    }

    /** 与收银一致：仅允许「商品上架」且「所属分类启用」 */
    private void assertProductSellable(Long productId) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new IllegalArgumentException("商品不存在，ID: " + productId);
        }
        if (product.getStatus() == null || product.getStatus() != 1) {
            throw new IllegalArgumentException("商品已下架，不可销售: " + product.getName());
        }
        if (product.getCategoryId() == null) {
            throw new IllegalArgumentException("商品未设置分类，不可销售: " + product.getName());
        }
        Category cat = categoryMapper.selectById(product.getCategoryId());
        if (cat == null || cat.getStatus() == null || cat.getStatus() != 1) {
            throw new IllegalArgumentException("商品所属分类已停用，不可销售: " + product.getName());
        }
    }

    private static Long parseLong(Object o) {
        if (o == null) {
            return null;
        }
        if (o instanceof Number) {
            return ((Number) o).longValue();
        }
        String s = o.toString().trim();
        if (s.isEmpty()) {
            return null;
        }
        return Long.parseLong(s);
    }

    private static double lineUnitPrice(Map<String, Object> item) {
        Object u = item.get("unitPrice");
        if (u == null) {
            u = item.get("price");
        }
        if (u == null) {
            throw new IllegalArgumentException("订单项缺少单价 unitPrice/price");
        }
        return Double.parseDouble(u.toString());
    }

    private String generateOrderNumber() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        String timeStr = sdf.format(new Date());
        Random random = new Random();
        String randomStr = String.format("%04d", random.nextInt(10000));
        return "ORD" + timeStr + randomStr;
    }
}
