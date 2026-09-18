package com.supermarket.backend.entity;

import lombok.Data;

import java.util.Date;

/**
 * 支付记录（支付流水单）。
 *
 * <p>一笔订单可能产生多条支付记录（首次支付失败后重新发起），
 * 但最多只有一条 {@code status=SUCCESS}。
 */
@Data
public class PaymentRecord {

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_SUCCESS = "SUCCESS";
    public static final String STATUS_FAILED = "FAILED";
    public static final String STATUS_CLOSED = "CLOSED";

    private Long id;

    /** 支付单号（业务唯一） */
    private String payNo;

    /** 订单ID */
    private Long orderId;

    /** 订单编号 */
    private String orderNumber;

    /** 支付金额 */
    private Double amount;

    /** 支付方式：WECHAT / ALIPAY / CASH / CARD */
    private String paymentMethod;

    /** 支付状态：PENDING / SUCCESS / FAILED / CLOSED */
    private String status;

    /** 支付渠道 */
    private String channel;

    /** 第三方交易号（模拟网关生成） */
    private String transactionId;

    /** 备注 */
    private String remark;

    private Date createTime;

    /** 支付完成时间 */
    private Date payTime;

    private Date updateTime;
}
