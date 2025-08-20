package com.example.ordersagaoutbox.domain.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @Slf4j
public class PaymentService {
    @Transactional
    public void authorize(String orderId, long amount){
        // 데모: 0 이하 금액이면 실패 던지기
        if (amount <= 0) throw new IllegalStateException("invalid amount");
        log.info("Payment authorized. orderId={}, amount={}", orderId, amount);
    }
}
