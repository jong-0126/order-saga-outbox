package com.example.ordersagaoutbox.domain.order.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Getter
public class OrderEntity {
    @Id private String id;
    @Column(nullable=false) private String productId;
    @Column(nullable=false) private int quantity;
    @Column(nullable=false) private long amount;
    @Enumerated(EnumType.STRING) @Column(nullable=false) private OrderStatus status;
    @Column(nullable=false) private Instant createdAt;

    public static OrderEntity newOrder(String productId, int quantity, long amount){
        OrderEntity o = new OrderEntity();
        o.id = UUID.randomUUID().toString();
        o.productId = productId; o.quantity = quantity; o.amount = amount;
        o.status = OrderStatus.NEW; o.createdAt = Instant.now();
        return o;
    }
}
