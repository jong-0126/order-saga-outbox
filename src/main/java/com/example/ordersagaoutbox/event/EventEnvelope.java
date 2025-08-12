package com.example.ordersagaoutbox.event;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter @Setter
public class EventEnvelope<T> {
    private String type;        // 예: OrderCreated
    private String correlationId; // 보통 orderId
    private T payload;
    private Instant createdAt;

    public EventEnvelope() {}
    public EventEnvelope(String type, String correlationId, T payload) {
        this.type = type;
        this.correlationId = correlationId;
        this.payload = payload;
        this.createdAt = Instant.now();
    }
}
