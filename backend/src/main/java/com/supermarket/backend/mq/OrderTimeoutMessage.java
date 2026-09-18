package com.supermarket.backend.mq;

/**
 * 订单超时延迟消息体。
 * 订单创建成功后投递，延迟 10 分钟后由消费者接收，
 * 消费者据此判断订单是否仍处于「未支付」状态，决定是否自动取消。
 */
public class OrderTimeoutMessage {

    /** 订单主键 */
    private Long orderId;

    /** 订单编号（便于日志排查） */
    private String orderNumber;

    /** 下单时间（毫秒时间戳），用于二次校验是否真的超时 */
    private Long createTimeMillis;

    /** 消息投递时间（毫秒时间戳） */
    private Long sentAtMillis;

    public OrderTimeoutMessage() {
    }

    public OrderTimeoutMessage(Long orderId, String orderNumber, Long createTimeMillis, Long sentAtMillis) {
        this.orderId = orderId;
        this.orderNumber = orderNumber;
        this.createTimeMillis = createTimeMillis;
        this.sentAtMillis = sentAtMillis;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public Long getCreateTimeMillis() {
        return createTimeMillis;
    }

    public void setCreateTimeMillis(Long createTimeMillis) {
        this.createTimeMillis = createTimeMillis;
    }

    public Long getSentAtMillis() {
        return sentAtMillis;
    }

    public void setSentAtMillis(Long sentAtMillis) {
        this.sentAtMillis = sentAtMillis;
    }

    @Override
    public String toString() {
        return "OrderTimeoutMessage{orderId=" + orderId
                + ", orderNumber='" + orderNumber + '\''
                + ", createTimeMillis=" + createTimeMillis
                + ", sentAtMillis=" + sentAtMillis + '}';
    }
}
