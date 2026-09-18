package com.supermarket.backend.payment;

import com.supermarket.backend.config.PaymentProperties;
import com.supermarket.backend.service.PaymentService;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 模拟支付网关。
 *
 * <p>项目没有真实商户资质，无法直接对接微信/支付宝，因此这里用一个「模拟网关」把
 * <b>完整的支付链路</b>跑通：<br>
 * 1) {@code createPrepay} 生成预支付单与支付参数（等价于统一下单接口）<br>
 * 2) 由 {@code /api/payments/{payNo}/confirm} 模拟网关<b>异步回调</b>通知支付结果<br>
 * 3) 也可以配置 {@code payment.mock.auto-confirm-delay-ms} 让网关自动回调，便于演示
 *
 * <p>接真实渠道时，只需把本类替换为对应的 SDK 调用（统一下单 + 回调验签），
 * {@code PaymentService} 的编排逻辑无需改动。
 */
@Component
public class MockPaymentGateway {

    private static final Logger log = LoggerFactory.getLogger(MockPaymentGateway.class);

    private final PaymentProperties properties;

    /** 延迟注入，避免 PaymentService 与网关形成构造期循环依赖 */
    private final ObjectProvider<PaymentService> paymentServiceProvider;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "mock-pay-gateway");
        t.setDaemon(true);
        return t;
    });

    public MockPaymentGateway(PaymentProperties properties, ObjectProvider<PaymentService> paymentServiceProvider) {
        this.properties = properties;
        this.paymentServiceProvider = paymentServiceProvider;
    }

    /** 渠道显示名 */
    public String channelName(String paymentMethod) {
        if (paymentMethod == null) {
            return "未知渠道";
        }
        switch (paymentMethod.toUpperCase()) {
            case "WECHAT":
                return "微信支付(模拟)";
            case "ALIPAY":
                return "支付宝(模拟)";
            case "CARD":
                return "银行卡(模拟)";
            case "CASH":
                return "现金";
            default:
                return paymentMethod + "(模拟)";
        }
    }

    /**
     * 模拟「统一下单」，返回前端所需的支付参数。
     *
     * @param payNo   商户支付单号
     * @param channel 渠道名
     * @param amount  金额
     * @return 支付参数（prepayId / codeUrl / expireSeconds 等）
     */
    public Map<String, Object> createPrepay(String payNo, String channel, double amount) {
        Map<String, Object> params = new LinkedHashMap<>();
        String prepayId = "MOCKPREPAY" + UUID.randomUUID().toString().replace("-", "").substring(0, 20);
        params.put("prepayId", prepayId);
        params.put("codeUrl", "mockpay://bizpayurl?pr=" + prepayId);
        params.put("channel", channel);
        params.put("amount", amount);
        params.put("expireSeconds", 600);
        params.put("mock", true);
        log.info("模拟网关统一下单成功: payNo={}, channel={}, amount={}, prepayId={}",
                payNo, channel, amount, prepayId);
        return params;
    }

    /** 生成模拟的第三方交易号 */
    public String generateTransactionId(String channel) {
        return "MOCK" + System.currentTimeMillis() + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }

    /**
     * 若配置了自动回调，则延迟调度一次「支付成功」回调（仅用于演示）。
     */
    public void scheduleAutoConfirm(String payNo, String channel) {
        long delay = properties.getMock().getAutoConfirmDelayMs();
        if (delay <= 0) {
            return;
        }
        scheduler.schedule(() -> {
            try {
                PaymentService service = paymentServiceProvider.getIfAvailable();
                if (service == null) {
                    return;
                }
                service.confirmPayment(payNo, generateTransactionId(channel), true, null);
                log.info("模拟网关已自动回调支付成功: payNo={}", payNo);
            } catch (Exception e) {
                log.warn("模拟网关自动回调失败: payNo={}, cause={}", payNo, e.getMessage());
            }
        }, delay, TimeUnit.MILLISECONDS);
    }

    @PreDestroy
    public void shutdown() {
        scheduler.shutdownNow();
    }
}
