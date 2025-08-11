package com.example.ordersagaoutbox.domain.payment.service;

import com.example.ordersagaoutbox.domain.payment.entity.PaymentAttemptEntity;
import com.example.ordersagaoutbox.domain.payment.repostiroy.PaymentAttemptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentAttemptRepository paymentAttemptRepository;

    @Transactional
    public void authorize(String orderId, long amount){
        // 데모 규칙: 홀수 금액이면 실패, 짝수 금액이면 승인
        boolean ok = (amount % 2 == 0);
        paymentAttemptRepository.save(PaymentAttemptEntity.of(orderId, amount, ok ? "AUTHORIZED":"FAILED"));
        if (!ok) throw new IllegalStateException("Payment failed (demo rule)");
    }
}
