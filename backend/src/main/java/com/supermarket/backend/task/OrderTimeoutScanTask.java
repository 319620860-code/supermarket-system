package com.supermarket.backend.task;

import com.supermarket.backend.entity.Order;
import com.supermarket.backend.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 订单超时未支付 —— 兜底扫描任务。
 *
 * <p>正常情况下超时取消由 RocketMQ 延迟消息驱动（实时、无轮询开销）。
 * 本任务作为<b>可靠性兜底</b>，覆盖以下场景：
 * <ul>
 *   <li>RocketMQ 不可用 / 发送失败（{@code OrderTimeoutProducer} 已降级）</li>
 *   <li>消息在 Broker 端丢失</li>
 *   <li>应用重启期间错过的延迟消息</li>
 * </ul>
 *
 * <p>因此即便在没有部署 RocketMQ 的环境（如小内存服务器），
 * 超时自动取消功能依然完整可用。可通过
 * {@code order.timeout.fallback-scan.enabled=false} 关闭。
 */
@Component
@ConditionalOnProperty(prefix = "order.timeout.fallback-scan", name = "enabled", havingValue = "true", matchIfMissing = true)
public class OrderTimeoutScanTask {

    private static final Logger log = LoggerFactory.getLogger(OrderTimeoutScanTask.class);

    /** 单批最大处理条数，避免单次扫描长时间占用连接 */
    private static final int BATCH_LIMIT = 200;

    private final OrderService orderService;

    public OrderTimeoutScanTask(OrderService orderService) {
        this.orderService = orderService;
    }

    @Scheduled(cron = "${order.timeout.fallback-scan.cron:0 */1 * * * ?}")
    public void scanTimeoutOrders() {
        List<Order> candidates = orderService.findTimeoutUnpaidOrders(BATCH_LIMIT);
        if (candidates.isEmpty()) {
            return;
        }
        log.info("兜底扫描发现 {} 笔超时未支付订单，开始处理", candidates.size());
        int cancelled = 0;
        for (Order order : candidates) {
            try {
                // 经由 Spring 代理调用，保证每笔订单独立事务
                if (orderService.autoCancelIfTimeout(order.getId())) {
                    cancelled++;
                }
            } catch (Exception e) {
                log.error("兜底扫描取消订单失败: orderId={}, orderNumber={}",
                        order.getId(), order.getOrderNumber(), e);
            }
        }
        if (cancelled > 0) {
            log.info("兜底扫描完成，本次自动取消 {} 笔超时订单", cancelled);
        }
    }
}
