package com.example.ordersagaoutbox.domain.repostiroy;

import com.example.ordersagaoutbox.domain.entity.PaymentAttemptEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentAttemptRepository extends JpaRepository<PaymentAttemptEntity, String> {
}
