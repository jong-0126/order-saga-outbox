package com.example.ordersagaoutbox.domain.payment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payment_attempt")
@Getter
public class PaymentAttemptEntity {
    @Id
    private String id;
    @Column(nullable=false) private String orderId;
    @Column(nullable=false) private long amount;
    @Column(nullable=false) private String status; // AUTHORIZED or FAILED
    @Column(nullable=false) private Instant createdAt;

    public static PaymentAttemptEntity of(String orderId, long amount, String status){
        PaymentAttemptEntity p = new PaymentAttemptEntity();
        p.id = UUID.randomUUID().toString();
        p.orderId = orderId; p.amount = amount; p.status = status;
        p.createdAt = Instant.now();
        return p;
    }
}
