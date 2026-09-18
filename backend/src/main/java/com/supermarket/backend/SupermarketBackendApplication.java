package com.supermarket.backend;

import com.supermarket.backend.config.RocketMqAvailabilityInitializer;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.supermarket.backend.mapper")
@EnableCaching
@EnableScheduling
public class SupermarketBackendApplication {

	public static void main(String[] args) {
		SpringApplication application = new SpringApplication(SupermarketBackendApplication.class);
		// 启动前探测 RocketMQ 可用性：不可达时自动关闭 MQ，避免中间件故障导致应用无法启动
		application.addInitializers(new RocketMqAvailabilityInitializer());
		application.run(args);
	}

}
