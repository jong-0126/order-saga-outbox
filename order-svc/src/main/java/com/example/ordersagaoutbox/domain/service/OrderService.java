package com.example.ordersagaoutbox.domain.service;

import com.example.common.events.EventEnvelope;
import com.example.common.events.EventTypes;
import com.example.common.events.order.OrderCreatedPayload;
import com.example.ordersagaoutbox.domain.dto.CreateOrderRequest;
import com.example.ordersagaoutbox.domain.entity.OrderEntity;
import com.example.ordersagaoutbox.domain.entity.OrderStatus;
import com.example.ordersagaoutbox.domain.outbox.service.OutboxService;
import com.example.ordersagaoutbox.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final OutboxService outbox;

    @Transactional
    public OrderEntity createIfAbsent(CreateOrderRequest req, String idemKey){
        return orderRepository.findByIdempotencyKey(idemKey).orElseGet(() -> {
            OrderEntity o = OrderEntity.newOrder(req.getProductId(), req.getQuantity(), req.getAmount(), idemKey);
            orderRepository.save(o);
            outbox.append(o.getId(), EventTypes.ORDER_CREATED,
                    EventEnvelope.of(EventTypes.ORDER_CREATED, o.getId(),
                            new OrderCreatedPayload(o.getId(), o.getProductId(), o.getQuantity(), o.getAmount(), "v1")));
            return o;
        });
    }

    @Transactional
    public void transition(String orderId, OrderStatus status, String eventType, Map<String,Object> payload) {
        OrderEntity o = orderRepository.findById(orderId).orElseThrow();
        o.setStatus(status); o.setUpdatedAt(Instant.now());
        outbox.append(orderId, eventType, EventEnvelope.of(eventType, orderId, payload));
    }
}
