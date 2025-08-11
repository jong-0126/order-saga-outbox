package com.example.ordersagaoutbox.domain.order.repository;

import com.example.ordersagaoutbox.domain.order.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<OrderEntity, String> {
    Optional<OrderEntity> findByIdempotencyKey(String idempotencyKey);
}
