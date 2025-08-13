package com.example.ordersagaoutbox;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication @EnableScheduling
public class PaymentApplication {
    public static void main(String[] args){ SpringApplication.run(PaymentApplication.class, args); }
}
