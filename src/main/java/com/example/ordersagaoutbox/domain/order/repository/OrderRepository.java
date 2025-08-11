package com.example.ordersagaoutbox.domain.order.repository;

import com.example.ordersagaoutbox.domain.order.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<OrderEntity, String> {
}
