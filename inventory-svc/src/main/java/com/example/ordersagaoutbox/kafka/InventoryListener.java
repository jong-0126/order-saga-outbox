package com.example.ordersagaoutbox.kafka;

import com.example.common.events.EventEnvelope;
import com.example.common.events.EventTypes;
import com.example.common.events.inventory.InventoryFailedPayload;
import com.example.common.events.inventory.InventoryReservedPayload;
import com.example.common.events.order.OrderCreatedPayload;
import com.example.ordersagaoutbox.domain.outbox.service.OutboxService;
import com.example.ordersagaoutbox.domain.service.InventoryService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class InventoryListener {
    private final ObjectMapper om;
    private final InventoryService inventory;
    private final OutboxService outbox;

    @KafkaListener(topics="${app.kafka.topic}", groupId="inventory-svc")
    @Transactional
    public void onMessage(String raw) throws Exception {
        EventEnvelope<?> base = om.readValue(raw, new TypeReference<EventEnvelope<Object>>(){});
        if (!EventTypes.ORDER_CREATED.equals(base.getType())) return;

        EventEnvelope<OrderCreatedPayload> env =
                om.readValue(raw, new TypeReference<EventEnvelope<OrderCreatedPayload>>(){});
        var p = env.getPayload();

        try {
            inventory.reserve(p.getProductId(), p.getQuantity());
            outbox.append(p.getOrderId(), EventTypes.INVENTORY_RESERVED,
                    EventEnvelope.of(EventTypes.INVENTORY_RESERVED, p.getOrderId(),
                            new InventoryReservedPayload(p.getOrderId(), p.getProductId(), p.getQuantity(), "v1")));
        } catch (Exception e) {
            outbox.append(p.getOrderId(), EventTypes.INVENTORY_FAILED,
                    EventEnvelope.of(EventTypes.INVENTORY_FAILED, p.getOrderId(),
                            new InventoryFailedPayload(p.getOrderId(), e.getMessage(), "v1")));
        }
    }

}
