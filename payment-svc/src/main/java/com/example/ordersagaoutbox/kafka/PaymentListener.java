package com.example.ordersagaoutbox.kafka;

import com.example.common.events.EventEnvelope;
import com.example.common.events.EventTypes;
import com.example.common.events.inventory.InventoryReservedPayload;
import com.example.common.events.payment.PaymentAuthorizedPayload;
import com.example.common.events.payment.PaymentFailedPayload;
import com.example.ordersagaoutbox.domain.outbox.service.OutboxService;
import com.example.ordersagaoutbox.domain.service.PaymentService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component @RequiredArgsConstructor
public class PaymentListener {
    private final ObjectMapper objectMapper;
    private final PaymentService paymentService;
    private final OutboxService outboxService;

    @KafkaListener(topics="${app.kafka.topic}", groupId="payment-svc")
    @Transactional
    public void onMessage(String raw) throws Exception {
        EventEnvelope<?> base = objectMapper.readValue(raw, new TypeReference<EventEnvelope<Object>>(){});
        if (!EventTypes.INVENTORY_RESERVED.equals(base.getType())) return;

        EventEnvelope<InventoryReservedPayload> env =
                objectMapper.readValue(raw, new TypeReference<EventEnvelope<InventoryReservedPayload>>(){});
        var p = env.getPayload();

        try {
            long amount = 10_000L; // 데모: 금액은 임시로 고정/또는 메시지에 포함시켜도 됨
            paymentService.authorize(p.getOrderId(), amount);
            outboxService.append(p.getOrderId(), EventTypes.PAYMENT_AUTHORIZED,
                    EventEnvelope.of(EventTypes.PAYMENT_AUTHORIZED, p.getOrderId(),
                            new PaymentAuthorizedPayload(p.getOrderId(), amount, "v1")));
        } catch (Exception e) {
            outboxService.append(p.getOrderId(), EventTypes.PAYMENT_FAILED,
                    EventEnvelope.of(EventTypes.PAYMENT_FAILED, p.getOrderId(),
                            new PaymentFailedPayload(p.getOrderId(), e.getMessage(), "v1")));
        }
    }
}
