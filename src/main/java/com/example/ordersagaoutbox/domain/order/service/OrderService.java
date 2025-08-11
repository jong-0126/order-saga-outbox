package com.example.ordersagaoutbox.domain.order.service;

import com.example.ordersagaoutbox.domain.order.dto.CreateOrderRequest;
import com.example.ordersagaoutbox.domain.order.entity.OrderEntity;
import com.example.ordersagaoutbox.domain.order.entity.OrderStatus;
import com.example.ordersagaoutbox.domain.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;

    @Transactional
    public OrderEntity createIfAbsent(CreateOrderRequest req, String idemKey){
        return orderRepository.findByIdempotencyKey(idemKey)
                .orElseGet(() -> orderRepository.save(OrderEntity.newOrder(
                        req.getProductId(), req.getQuantity(), req.getAmount(), idemKey
                )));
    }

    @Transactional
    public void markStatus(String orderId, OrderStatus status){
        orderRepository.findById(orderId).ifPresent(o -> {
            o.setStatus(status);
            o.setUpdatedAt(Instant.now());
        });
    }
}
