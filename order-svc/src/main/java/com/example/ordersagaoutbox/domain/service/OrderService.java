package com.example.ordersagaoutbox.domain.service;

import com.example.common.events.EventEnvelope;
import com.example.ordersagaoutbox.domain.dto.CreateOrderRequest;
import com.example.ordersagaoutbox.domain.entity.OrderEntity;
import com.example.ordersagaoutbox.domain.entity.OrderStatus;
import com.example.ordersagaoutbox.domain.outbox.service.OutboxService;
import com.example.ordersagaoutbox.domain.repository.OrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final OutboxService outboxService;
    private final ObjectMapper objectMapper;

    @Transactional
    public OrderEntity createIfAbsent(CreateOrderRequest req, String idemKey){
        return orderRepository.findByIdempotencyKey(idemKey).orElseGet(() -> {
            OrderEntity o = OrderEntity.newOrder(req.getProductId(), req.getQuantity(), req.getAmount(), idemKey);
            orderRepository.save(o);
            // 같은 트랜잭션에서 '주문 생성' 이벤트 기록
            outboxService.append(o.getId(), "OrderCreated",
                    EventEnvelope.of("OrderCreated", o.getId(),
                            Map.of("orderId", o.getId(), "productId", o.getProductId(),
                                    "quantity", o.getQuantity(), "amount", o.getAmount())
                    )
            );
            return o;
        });
    }

    @Transactional
    public void markStatus(String orderId, OrderStatus status){
        orderRepository.findById(orderId).ifPresent(o -> {
            o.setStatus(status);
            o.setUpdatedAt(Instant.now());
        });
    }

    @Transactional
    public void transition(String orderId, OrderStatus status, String eventType, Map<String,Object> payload) {
        OrderEntity o = orderRepository.findById(orderId).orElseThrow();
        o.setStatus(status); o.setUpdatedAt(Instant.now());
        outboxService.append(orderId, eventType, new EventEnvelope<>(eventType, orderId, payload, Instant.now()));
    }
}
