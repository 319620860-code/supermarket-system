package com.supermarket.backend;

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
		SpringApplication.run(SupermarketBackendApplication.class, args);
	}

}
