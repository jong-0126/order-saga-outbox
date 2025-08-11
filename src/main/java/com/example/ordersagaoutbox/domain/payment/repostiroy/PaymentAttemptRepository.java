package com.example.ordersagaoutbox.domain.payment.repostiroy;

import com.example.ordersagaoutbox.domain.payment.entity.PaymentAttemptEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentAttemptRepository extends JpaRepository<PaymentAttemptEntity, String> {
}
