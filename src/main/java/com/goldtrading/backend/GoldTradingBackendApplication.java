package com.goldtrading.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class GoldTradingBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(GoldTradingBackendApplication.class, args);
    }
}

