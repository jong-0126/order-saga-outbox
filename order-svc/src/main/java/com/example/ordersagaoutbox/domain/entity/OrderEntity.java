package com.example.ordersagaoutbox.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "orders")
public class OrderEntity {
    @Id
    private String id;

    @Column(nullable=false) private String productId;
    @Column(nullable=false) private int quantity;
    @Column(nullable=false) private long amount;

    @Enumerated(EnumType.STRING) @Column(nullable=false)
    private OrderStatus status;

    @Column(nullable=false, unique = true)          // 멱등키
    private String idempotencyKey;

    @Column(nullable=false) private Instant createdAt;
    @Column(nullable = false) private Instant updatedAt;

    public static OrderEntity newOrder(String productId, int quantity, long amount, String idem){
        OrderEntity o = new OrderEntity();
        o.id = UUID.randomUUID().toString();
        o.productId = productId; o.quantity = quantity; o.amount = amount;
        o.status = OrderStatus.NEW;
        o.idempotencyKey = idem;
        o.createdAt = Instant.now(); o.updatedAt = o.createdAt;
        return o;
    }
}
