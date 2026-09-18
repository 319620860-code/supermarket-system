package com.supermarket.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 订单超时相关配置（order.timeout.*）。
 */
@Component
@ConfigurationProperties(prefix = "order.timeout")
public class OrderTimeoutProperties {

    /** 订单支付超时时间（分钟），默认 10 */
    private int minutes = 10;

    /**
     * RocketMQ 延迟等级：
     * 1=1s 2=5s 3=10s 4=30s 5=1m 6=2m 7=3m 8=4m 9=5m 10=6m
     * 11=7m 12=8m 13=9m 14=10m 15=20m 16=30m 17=1h 18=2h
     */
    private int delayLevel = 14;

    private Mq mq = new Mq();

    private FallbackScan fallbackScan = new FallbackScan();

    public static class Mq {
        /** 是否启用 RocketMQ 发送/消费 */
        private boolean enabled = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    public static class FallbackScan {
        /** 兜底扫描开关：MQ 不可用或消息丢失时仍能自动取消超时订单 */
        private boolean enabled = true;

        /** 扫描 cron 表达式 */
        private String cron = "0 */1 * * * ?";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getCron() {
            return cron;
        }

        public void setCron(String cron) {
            this.cron = cron;
        }
    }

    public int getMinutes() {
        return minutes;
    }

    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }

    public int getDelayLevel() {
        return delayLevel;
    }

    public void setDelayLevel(int delayLevel) {
        this.delayLevel = delayLevel;
    }

    public Mq getMq() {
        return mq;
    }

    public void setMq(Mq mq) {
        this.mq = mq;
    }

    public FallbackScan getFallbackScan() {
        return fallbackScan;
    }

    public void setFallbackScan(FallbackScan fallbackScan) {
        this.fallbackScan = fallbackScan;
    }
}
