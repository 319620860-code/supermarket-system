package com.supermarket.backend.service;

import com.supermarket.backend.entity.PaymentRecord;

import java.util.List;
import java.util.Map;

public interface PaymentService {

    /**
     * 发起支付：为指定订单创建支付单并返回支付参数。
     *
     * <p>若该订单已有进行中的支付单（PENDING）则直接复用，不会重复创建。
     *
     * @param orderId       订单ID
     * @param paymentMethod 支付方式（WECHAT/ALIPAY/CASH/CARD），为空默认 WECHAT
     * @param remark        备注
     * @return 支付单信息 + payParams（模拟网关统一下单返回的支付参数）
     */
    Map<String, Object> createPayment(Long orderId, String paymentMethod, String remark);

    /**
     * 支付结果确认（模拟支付网关异步回调入口），幂等。
     *
     * @param payNo         支付单号
     * @param transactionId 第三方交易号（为空则自动生成）
     * @param success       网关是否通知支付成功
     * @param failReason    失败原因（success=false 时使用）
     * @return 处理结果，含 success 与 message
     */
    Map<String, Object> confirmPayment(String payNo, String transactionId, boolean success, String failReason);

    /** 按支付单号查询 */
    PaymentRecord getByPayNo(String payNo);

    /** 查询订单的全部支付记录（按时间倒序） */
    List<PaymentRecord> listByOrderId(Long orderId);
}
