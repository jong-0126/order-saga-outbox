package com.example.ordersagaoutbox.kafka;

import com.example.common.events.EventEnvelope;
import com.example.common.events.EventTypes;
import com.example.common.events.payment.PaymentAuthorizedPayload;
import com.example.ordersagaoutbox.domain.entity.OrderStatus;
import com.example.ordersagaoutbox.domain.service.OrderService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class OrderResultListener {
    private final ObjectMapper om;
    private final OrderService orderService;

    @KafkaListener(topics="${app.kafka.topic}", groupId="order-svc")
    @Transactional
    public void onMessage(String raw) throws Exception {
        EventEnvelope<?> base = om.readValue(raw, new TypeReference<EventEnvelope<Object>>(){});
        switch (base.getType()) {
            case EventTypes.PAYMENT_AUTHORIZED -> {
                EventEnvelope<PaymentAuthorizedPayload> env =
                        om.readValue(raw, new TypeReference<EventEnvelope<PaymentAuthorizedPayload>>(){});
                orderService.transition(env.getPayload().getOrderId(), OrderStatus.COMPLETED,
                        EventTypes.PAYMENT_AUTHORIZED, Map.of("orderId", env.getPayload().getOrderId()));
            }
            case EventTypes.INVENTORY_FAILED, EventTypes.PAYMENT_FAILED -> {
                orderService.transition(base.getCorrelationId(), OrderStatus.CANCELLED,
                        base.getType(), Map.of("orderId", base.getCorrelationId()));
            }
            default -> {}
        }
    }
}
