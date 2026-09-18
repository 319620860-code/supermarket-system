package com.supermarket.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 支付相关配置（payment.*）。
 */
@Component
@ConfigurationProperties(prefix = "payment")
public class PaymentProperties {

    private Mock mock = new Mock();

    public static class Mock {
        /** 是否启用模拟支付网关（无真实商户资质时用于演示完整支付链路） */
        private boolean enabled = true;

        /**
         * 模拟网关自动回调延迟（毫秒）。0 表示不自动回调（由调用方显式调用 confirm 接口）。
         * 开启后会在创建支付单 N 毫秒后自动回调「支付成功」，便于演示。
         */
        private long autoConfirmDelayMs = 0L;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public long getAutoConfirmDelayMs() {
            return autoConfirmDelayMs;
        }

        public void setAutoConfirmDelayMs(long autoConfirmDelayMs) {
            this.autoConfirmDelayMs = autoConfirmDelayMs;
        }
    }

    public Mock getMock() {
        return mock;
    }

    public void setMock(Mock mock) {
        this.mock = mock;
    }
}
