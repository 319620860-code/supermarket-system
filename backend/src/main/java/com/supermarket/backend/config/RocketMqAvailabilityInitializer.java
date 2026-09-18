package com.supermarket.backend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * RocketMQ 可用性探测器（在应用启动早期执行）。
 *
 * <p><b>解决的问题</b>：rocketmq-spring 的消费者容器在启动时会同步连接 NameServer，
 * 一旦 NameServer 不可达就会抛出 {@code RemotingConnectException}，
 * 进而导致整个 Spring 上下文启动失败 —— 即「消息中间件挂掉把业务应用一起拖垮」。
 *
 * <p><b>做法</b>：在容器 refresh 之前对 {@code rocketmq.name-server} 做一次 TCP 探活，
 * 不可达时把 {@code order.timeout.mq.enabled} 动态置为 false，
 * 使 MQ 相关的生产者/消费者 Bean 都不创建，应用照常启动，
 * 订单超时取消改由兜底扫描任务保证。Broker 恢复后重启应用即可重新启用 MQ。
 */
public class RocketMqAvailabilityInitializer
        implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final Logger log = LoggerFactory.getLogger(RocketMqAvailabilityInitializer.class);

    private static final int PROBE_TIMEOUT_MS = 1500;

    private static final String PROP_MQ_ENABLED = "order.timeout.mq.enabled";
    private static final String PROP_NAME_SERVER = "rocketmq.name-server";

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ConfigurableEnvironment env = applicationContext.getEnvironment();

        if (!Boolean.parseBoolean(env.getProperty(PROP_MQ_ENABLED, "true"))) {
            log.info("order.timeout.mq.enabled=false，跳过 RocketMQ 可用性探测");
            return;
        }

        String nameServer = env.getProperty(PROP_NAME_SERVER);
        if (nameServer == null || nameServer.trim().isEmpty()) {
            disableMq(env, "未配置 " + PROP_NAME_SERVER);
            return;
        }

        for (String addr : nameServer.split("[;,]")) {
            String target = addr.trim();
            if (target.isEmpty()) {
                continue;
            }
            if (probe(target)) {
                log.info("RocketMQ NameServer 可达（{}），启用 MQ 方式的订单超时取消", target);
                return;
            }
        }
        disableMq(env, "NameServer 不可达: " + nameServer);
    }

    /** 对 host:port 做一次 TCP 连接探测 */
    private boolean probe(String address) {
        int idx = address.lastIndexOf(':');
        String host = idx > 0 ? address.substring(0, idx) : address;
        int port = 9876;
        if (idx > 0) {
            try {
                port = Integer.parseInt(address.substring(idx + 1).trim());
            } catch (NumberFormatException ignored) {
                // 端口解析失败则使用默认 9876
            }
        }
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), PROBE_TIMEOUT_MS);
            return true;
        } catch (Exception e) {
            log.debug("RocketMQ NameServer 探测失败 {}:{} -> {}", host, port, e.getMessage());
            return false;
        }
    }

    private void disableMq(ConfigurableEnvironment env, String reason) {
        log.warn("RocketMQ 不可用（{}）。已自动关闭 MQ 生产者/消费者，应用照常启动，"
                + "订单超时未支付取消改由兜底扫描任务保证；Broker 恢复后重启应用即可重新启用。", reason);
        Map<String, Object> override = new HashMap<>();
        override.put(PROP_MQ_ENABLED, "false");
        env.getPropertySources().addFirst(new MapPropertySource("rocketMqAvailabilityOverride", override));
    }
}
