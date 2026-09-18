package com.supermarket.backend.mq;

/**
 * 订单相关 MQ 常量。
 */
public final class OrderMqConstants {

    /** 订单超时取消主题 */
    public static final String ORDER_TIMEOUT_TOPIC = "supermarket-order-timeout-topic";

    /** 消息标签 */
    public static final String TAG_ORDER_TIMEOUT = "order-timeout";

    private OrderMqConstants() {
    }
}
