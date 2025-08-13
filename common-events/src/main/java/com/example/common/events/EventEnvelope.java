package com.example.common.events;

import java.time.Instant;

public class EventEnvelope<T> {
    private String type;
    private String correlationId;
    private T payload;
    private Instant createdAt;

    public EventEnvelope() {} // Jackson용

    public EventEnvelope(String type, String correlationId, T payload, Instant createdAt) {
        this.type = type;
        this.correlationId = correlationId;
        this.payload = payload;
        this.createdAt = createdAt;
    }

    public static <T> EventEnvelope<T> of(String type, String corr, T payload) {
        return new EventEnvelope<>(type, corr, payload, Instant.now());
    }

    // getters/setters
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }

    public T getPayload() { return payload; }
    public void setPayload(T payload) { this.payload = payload; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
