-- =====================================================================
-- 支付记录表 + 超时扫描索引
-- 适用：MySQL 8.0
-- =====================================================================

USE supermarket;

CREATE TABLE IF NOT EXISTS payment_record (
    id             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    pay_no         VARCHAR(64)  NOT NULL COMMENT '支付单号（业务唯一）',
    order_id       BIGINT       NOT NULL COMMENT '订单ID',
    order_number   VARCHAR(50)  NOT NULL COMMENT '订单编号',
    amount         DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '支付金额',
    payment_method VARCHAR(20)  NOT NULL COMMENT '支付方式：WECHAT/ALIPAY/CASH/CARD',
    status         VARCHAR(20)  NOT NULL COMMENT '支付状态：PENDING/SUCCESS/FAILED/CLOSED',
    channel        VARCHAR(32)  DEFAULT NULL COMMENT '支付渠道',
    transaction_id VARCHAR(64)  DEFAULT NULL COMMENT '第三方交易号',
    remark         VARCHAR(255) DEFAULT NULL COMMENT '备注',
    create_time    DATETIME     NOT NULL COMMENT '创建时间',
    pay_time       DATETIME     DEFAULT NULL COMMENT '支付完成时间',
    update_time    DATETIME     NOT NULL COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_pay_no (pay_no),
    KEY idx_pay_order_id (order_id),
    KEY idx_pay_status (status)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '支付记录（支付流水单）';

-- 超时未支付扫描走 (payment_status, order_status, create_time) 组合索引
-- 若已存在同名索引会报 1061，可忽略
ALTER TABLE `order` ADD INDEX idx_pay_status_create (payment_status, order_status, create_time);
