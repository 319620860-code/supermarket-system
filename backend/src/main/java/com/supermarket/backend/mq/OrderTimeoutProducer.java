package com.supermarket.backend.mq;

import com.supermarket.backend.config.OrderTimeoutProperties;
import com.supermarket.backend.entity.Order;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

/**
 * 订单超时延迟消息生产者。
 *
 * <p>订单创建成功后，投递一条延迟消息（默认延迟等级 14 → 10 分钟）。
 * 消息到期后由 {@link OrderTimeoutConsumer} 消费，判断订单是否仍未支付，从而自动取消。
 *
 * <p>可靠性设计：发送失败**不会**影响下单主流程，仅记录告警日志；
 * 超时取消由兜底扫描任务（{@code OrderTimeoutScanTask}）保证最终一致。
 */
@Component
@ConditionalOnProperty(prefix = "order.timeout.mq", name = "enabled", havingValue = "true", matchIfMissing = true)
public class OrderTimeoutProducer {

    private static final Logger log = LoggerFactory.getLogger(OrderTimeoutProducer.class);

    /** 同步发送超时时间（毫秒） */
    private static final long SEND_TIMEOUT_MS = 3000L;

    private final RocketMQTemplate rocketMQTemplate;
    private final OrderTimeoutProperties properties;

    public OrderTimeoutProducer(RocketMQTemplate rocketMQTemplate, OrderTimeoutProperties properties) {
        this.rocketMQTemplate = rocketMQTemplate;
        this.properties = properties;
    }

    /**
     * 发送订单超时延迟消息。
     *
     * @param order 已创建的订单
     * @return 是否发送成功（失败时已降级，不影响调用方）
     */
    public boolean sendOrderTimeoutMessage(Order order) {
        if (order == null || order.getId() == null) {
            return false;
        }
        long createMillis = order.getCreateTime() != null ? order.getCreateTime().getTime() : System.currentTimeMillis();
        OrderTimeoutMessage payload = new OrderTimeoutMessage(
                order.getId(), order.getOrderNumber(), createMillis, System.currentTimeMillis());

        String destination = OrderMqConstants.ORDER_TIMEOUT_TOPIC + ":" + OrderMqConstants.TAG_ORDER_TIMEOUT;
        try {
            Message<OrderTimeoutMessage> message = MessageBuilder.withPayload(payload).build();
            SendResult result = rocketMQTemplate.syncSend(
                    destination, message, SEND_TIMEOUT_MS, properties.getDelayLevel());
            log.info("订单超时延迟消息已投递: orderId={}, orderNumber={}, delayLevel={}, msgId={}",
                    order.getId(), order.getOrderNumber(), properties.getDelayLevel(), result.getMsgId());
            return true;
        } catch (Exception e) {
            // 降级：MQ 故障不得阻断下单，交由兜底扫描兜住
            log.warn("订单超时延迟消息投递失败（已降级，将由兜底扫描处理）: orderId={}, orderNumber={}, cause={}",
                    order.getId(), order.getOrderNumber(), e.getMessage());
            return false;
        }
    }
}
