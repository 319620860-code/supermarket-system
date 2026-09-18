package com.supermarket.backend.controller;

import com.supermarket.backend.entity.PaymentRecord;
import com.supermarket.backend.service.AuditLogService;
import com.supermarket.backend.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 支付相关接口。
 *
 * <p>典型调用顺序：<br>
 * 1) {@code POST /api/payments/create} 发起支付，拿到 payNo 与支付参数<br>
 * 2) 用户完成支付后，网关回调 {@code POST /api/payments/{payNo}/confirm}（本项目为模拟网关）<br>
 * 3) 可通过 {@code GET /api/payments/order/{orderId}} 查询订单的支付流水
 */
@RestController
@RequestMapping("/api/payments")
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_CASHIER')")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private AuditLogService auditLogService;

    /**
     * 发起支付：创建支付单并返回支付参数。
     * 请求体：{ "orderId": 1, "paymentMethod": "WECHAT", "remark": "可选" }
     */
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> create(@RequestBody Map<String, Object> request) {
        Object orderIdObj = request.get("orderId");
        if (orderIdObj == null) {
            throw new IllegalArgumentException("orderId 不能为空");
        }
        Long orderId = Long.parseLong(orderIdObj.toString());
        String paymentMethod = request.get("paymentMethod") == null ? null : request.get("paymentMethod").toString();
        String remark = request.get("remark") == null ? null : request.get("remark").toString();

        Map<String, Object> result = paymentService.createPayment(orderId, paymentMethod, remark);
        auditLogService.log("PAYMENT_CREATE",
                "orderId=" + orderId + ",payNo=" + result.get("payNo") + ",method=" + result.get("paymentMethod"));
        return ResponseEntity.ok(result);
    }

    /**
     * 支付结果确认（模拟支付网关异步回调入口），幂等。
     * 请求体：{ "transactionId": "可选", "success": true, "failReason": "可选" }
     */
    @PostMapping("/{payNo}/confirm")
    public ResponseEntity<Map<String, Object>> confirm(@PathVariable String payNo,
                                                       @RequestBody(required = false) Map<String, Object> request) {
        Map<String, Object> body = request == null ? new java.util.HashMap<>() : request;
        String transactionId = body.get("transactionId") == null ? null : body.get("transactionId").toString();
        boolean success = body.get("success") == null || Boolean.parseBoolean(body.get("success").toString());
        String failReason = body.get("failReason") == null ? null : body.get("failReason").toString();

        Map<String, Object> result = paymentService.confirmPayment(payNo, transactionId, success, failReason);
        auditLogService.log("PAYMENT_CONFIRM",
                "payNo=" + payNo + ",success=" + result.get("success") + ",status=" + result.get("status"));
        return ResponseEntity.ok(result);
    }

    /** 按支付单号查询 */
    @GetMapping("/{payNo}")
    public ResponseEntity<PaymentRecord> getByPayNo(@PathVariable String payNo) {
        PaymentRecord record = paymentService.getByPayNo(payNo);
        if (record == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(record);
    }

    /** 查询某订单的全部支付流水 */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<PaymentRecord>> listByOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(paymentService.listByOrderId(orderId));
    }
}
