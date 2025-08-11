package com.example.ordersagaoutbox.domain.order.service;

import com.example.ordersagaoutbox.domain.order.dto.CreateOrderRequest;
import com.example.ordersagaoutbox.domain.order.entity.OrderEntity;
import com.example.ordersagaoutbox.domain.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;

    @Transactional
    public OrderEntity create(CreateOrderRequest req){
        return orderRepository.save(OrderEntity.newOrder(req.getProductId(), req.getQuantity(), req.getAmount()));
    }
}
