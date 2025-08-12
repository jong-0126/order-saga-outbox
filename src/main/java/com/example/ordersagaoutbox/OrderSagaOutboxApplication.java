package com.example.ordersagaoutbox;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class OrderSagaOutboxApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderSagaOutboxApplication.class, args);
    }

}
