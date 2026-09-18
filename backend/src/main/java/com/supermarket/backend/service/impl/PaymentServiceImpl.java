package com.supermarket.backend.service.impl;

import com.supermarket.backend.entity.Order;
import com.supermarket.backend.entity.PaymentRecord;
import com.supermarket.backend.mapper.PaymentRecordMapper;
import com.supermarket.backend.payment.MockPaymentGateway;
import com.supermarket.backend.service.OrderService;
import com.supermarket.backend.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 支付业务实现。
 *
 * <p>整体链路：<br>
 * {@code createPayment} 创建 PENDING 支付单（调用模拟网关统一下单）
 * → 用户完成支付
 * → {@code confirmPayment} 接收网关异步回调
 * → 校验订单状态 → 幂等占位（PENDING→SUCCESS）→ 调用 {@code OrderService#payOrder}
 * 扣减库存并置订单为 PAID/COMPLETED。
 *
 * <p>幂等性：<br>
 * 1) 支付单通过 {@code markSuccessIfPending} 条件更新占位，重复回调只有一个能生效；<br>
 * 2) 订单通过 {@code OrderMapper#payIfUnpaid} 条件更新占位，避免库存被重复扣减。
 */
@Service
public class PaymentServiceImpl implements PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentServiceImpl.class);

    private static final Set<String> SUPPORTED_METHODS =
            new LinkedHashSet<>(Arrays.asList("WECHAT", "ALIPAY", "CASH", "CARD"));

    @Autowired
    private PaymentRecordMapper paymentRecordMapper;

    @Autowired
    private OrderService orderService;

    @Autowired
    private MockPaymentGateway mockPaymentGateway;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createPayment(Long orderId, String paymentMethod, String remark) {
        if (orderId == null) {
            throw new IllegalArgumentException("订单ID不能为空");
        }
        Order order = orderService.getById(orderId);
        if (order == null) {
            throw new IllegalStateException("订单不存在");
        }
        if (!"UNPAID".equals(order.getPaymentStatus())) {
            throw new IllegalStateException("订单已支付或已退款，无需再次支付");
        }
        if (!"CREATED".equals(order.getOrderStatus())) {
            throw new IllegalStateException("订单已取消或已完成，无法支付");
        }

        String method = (paymentMethod == null || paymentMethod.trim().isEmpty())
                ? "WECHAT" : paymentMethod.trim().toUpperCase(Locale.ROOT);
        if (!SUPPORTED_METHODS.contains(method)) {
            throw new IllegalArgumentException("不支持的支付方式: " + paymentMethod + "，可选 " + SUPPORTED_METHODS);
        }

        // 复用进行中的支付单，避免重复下单
        PaymentRecord pending = paymentRecordMapper.selectPendingByOrderId(orderId);
        if (pending != null) {
            log.info("订单已存在进行中的支付单，直接复用: orderNumber={}, payNo={}",
                    order.getOrderNumber(), pending.getPayNo());
            Map<String, Object> params = mockPaymentGateway.createPrepay(
                    pending.getPayNo(), pending.getChannel(), pending.getAmount());
            return buildResult(pending, params);
        }

        PaymentRecord record = new PaymentRecord();
        record.setPayNo(generatePayNo());
        record.setOrderId(orderId);
        record.setOrderNumber(order.getOrderNumber());
        record.setAmount(order.getActualAmount() != null ? order.getActualAmount() : 0.0);
        record.setPaymentMethod(method);
        record.setStatus(PaymentRecord.STATUS_PENDING);
        record.setChannel(mockPaymentGateway.channelName(method));
        record.setRemark(remark);
        Date now = new Date();
        record.setCreateTime(now);
        record.setUpdateTime(now);
        paymentRecordMapper.insert(record);

        Map<String, Object> payParams = mockPaymentGateway.createPrepay(
                record.getPayNo(), record.getChannel(), record.getAmount());
        // 演示用：如配置了自动回调延迟，则由模拟网关自动回调「支付成功」
        mockPaymentGateway.scheduleAutoConfirm(record.getPayNo(), method);

        log.info("支付单创建成功: payNo={}, orderNumber={}, amount={}, method={}",
                record.getPayNo(), record.getOrderNumber(), record.getAmount(), method);
        return buildResult(record, payParams);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> confirmPayment(String payNo, String transactionId, boolean success, String failReason) {
        if (payNo == null || payNo.trim().isEmpty()) {
            throw new IllegalArgumentException("支付单号不能为空");
        }
        PaymentRecord pay = paymentRecordMapper.selectByPayNo(payNo);
        if (pay == null) {
            throw new IllegalStateException("支付单不存在: " + payNo);
        }

        // ---- 幂等：已完成/已关闭/已失败 的支付单直接返回，不重复处理 ----
        if (PaymentRecord.STATUS_SUCCESS.equals(pay.getStatus())) {
            return result(pay, true, "支付单已完成（重复回调已忽略）");
        }
        if (PaymentRecord.STATUS_CLOSED.equals(pay.getStatus())) {
            return result(pay, false, "支付单已关闭，订单可能已取消或超时");
        }
        if (PaymentRecord.STATUS_FAILED.equals(pay.getStatus())) {
            return result(pay, false, "支付单已失败，请重新发起支付");
        }

        Date now = new Date();

        // ---- 网关通知支付失败 ----
        if (!success) {
            pay.setStatus(PaymentRecord.STATUS_FAILED);
            pay.setRemark(failReason);
            pay.setUpdateTime(now);
            paymentRecordMapper.update(pay);
            log.info("支付失败: payNo={}, reason={}", payNo, failReason);
            return result(pay, false, "支付失败：" + (failReason == null ? "网关返回失败" : failReason));
        }

        // ---- 支付成功前，先校验订单是否仍可支付，避免为已取消订单记账 ----
        Order order = orderService.getById(pay.getOrderId());
        if (order == null) {
            throw new IllegalStateException("订单不存在: " + pay.getOrderId());
        }
        if (!"UNPAID".equals(order.getPaymentStatus()) || !"CREATED".equals(order.getOrderStatus())) {
            pay.setStatus(PaymentRecord.STATUS_CLOSED);
            pay.setRemark("订单状态已变更(" + order.getOrderStatus() + "/" + order.getPaymentStatus() + ")，支付单关闭");
            pay.setUpdateTime(now);
            paymentRecordMapper.update(pay);
            log.warn("支付回调到达时订单已不可支付，支付单已关闭: payNo={}, orderStatus={}, paymentStatus={}",
                    payNo, order.getOrderStatus(), order.getPaymentStatus());
            return result(pay, false, "订单已取消或已支付，支付单已关闭");
        }

        String txId = (transactionId == null || transactionId.trim().isEmpty())
                ? mockPaymentGateway.generateTransactionId(pay.getPaymentMethod())
                : transactionId;

        // ---- 幂等占位：PENDING -> SUCCESS，并发/重复回调只有一个能成功 ----
        int claimed = paymentRecordMapper.markSuccessIfPending(payNo, txId, now);
        if (claimed == 0) {
            return result(paymentRecordMapper.selectByPayNo(payNo), true, "重复回调已忽略");
        }

        // ---- 驱动订单支付：扣减库存 + 置 PAID/COMPLETED（内部同样有条件更新保证幂等） ----
        Map<String, Object> payRequest = new HashMap<>();
        payRequest.put("paymentMethod", pay.getPaymentMethod());
        payRequest.put("amount", pay.getAmount());
        payRequest.put("remark", "支付单号:" + payNo);
        orderService.payOrder(pay.getOrderId(), payRequest);

        log.info("支付成功: payNo={}, orderNumber={}, amount={}, transactionId={}",
                payNo, pay.getOrderNumber(), pay.getAmount(), txId);
        return result(paymentRecordMapper.selectByPayNo(payNo), true, "支付成功");
    }

    @Override
    public PaymentRecord getByPayNo(String payNo) {
        return paymentRecordMapper.selectByPayNo(payNo);
    }

    @Override
    public List<PaymentRecord> listByOrderId(Long orderId) {
        if (orderId == null) {
            return Collections.emptyList();
        }
        List<PaymentRecord> list = paymentRecordMapper.selectByOrderId(orderId);
        return list == null ? Collections.emptyList() : list;
    }

    private Map<String, Object> buildResult(PaymentRecord record, Map<String, Object> payParams) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("payNo", record.getPayNo());
        result.put("orderId", record.getOrderId());
        result.put("orderNumber", record.getOrderNumber());
        result.put("amount", record.getAmount());
        result.put("paymentMethod", record.getPaymentMethod());
        result.put("status", record.getStatus());
        result.put("channel", record.getChannel());
        result.put("transactionId", record.getTransactionId());
        result.put("createTime", record.getCreateTime());
        result.put("payTime", record.getPayTime());
        if (payParams != null) {
            result.put("payParams", payParams);
        }
        return result;
    }

    private Map<String, Object> result(PaymentRecord record, boolean success, String message) {
        Map<String, Object> result = buildResult(record, null);
        result.put("success", success);
        result.put("message", message);
        return result;
    }

    private String generatePayNo() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        return "PAY" + sdf.format(new Date()) + String.format("%04d", new Random().nextInt(10000));
    }
}
