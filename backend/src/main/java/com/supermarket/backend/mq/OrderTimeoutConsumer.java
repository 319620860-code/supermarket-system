package com.supermarket.backend.mq;

import com.supermarket.backend.service.OrderService;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 订单超时延迟消息消费者。
 *
 * <p>RocketMQ 延迟消息到期后投递到此，判断订单是否仍为「未支付」：
 * <ul>
 *   <li>仍未支付 → 自动取消并释放锁定库存</li>
 *   <li>已支付/已取消 → 幂等忽略（用户及时付款的情况）</li>
 * </ul>
 *
 * <p>幂等性由 {@code OrderService#autoCancelIfTimeout} 内部的条件更新保证，
 * 因此 MQ 的重复投递/重试不会造成重复释放库存。
 */
@Component
@ConditionalOnProperty(prefix = "order.timeout.mq", name = "enabled", havingValue = "true", matchIfMissing = true)
@RocketMQMessageListener(
        topic = OrderMqConstants.ORDER_TIMEOUT_TOPIC,
        consumerGroup = "${rocketmq.consumer.group:supermarket-order-timeout-consumer}",
        selectorExpression = OrderMqConstants.TAG_ORDER_TIMEOUT)
public class OrderTimeoutConsumer implements RocketMQListener<OrderTimeoutMessage> {

    private static final Logger log = LoggerFactory.getLogger(OrderTimeoutConsumer.class);

    private final OrderService orderService;

    public OrderTimeoutConsumer(OrderService orderService) {
        this.orderService = orderService;
    }

    @Override
    public void onMessage(OrderTimeoutMessage message) {
        if (message == null || message.getOrderId() == null) {
            log.warn("收到空的订单超时消息，已忽略");
            return;
        }
        log.info("收到订单超时延迟消息: {}", message);
        try {
            boolean cancelled = orderService.autoCancelIfTimeout(message.getOrderId());
            log.info("订单超时消息处理完成: orderNumber={}, 是否执行取消={}",
                    message.getOrderNumber(), cancelled);
        } catch (Exception e) {
            // 向上抛出由 RocketMQ 按重试策略重投；业务方法幂等，重复消费安全
            log.error("处理订单超时消息异常，交由 MQ 重试: {}", message, e);
            throw e;
        }
    }
}
