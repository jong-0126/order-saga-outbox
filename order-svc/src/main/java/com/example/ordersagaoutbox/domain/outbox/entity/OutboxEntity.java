package com.example.ordersagaoutbox.domain.outbox.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "order_outbox")
@Getter @Setter
public class OutboxEntity {
    public enum Status { PENDING, SENT, FAILED }

    @Id private String id;
    @Column(nullable=false) private String aggregateId; // orderId
    @Column(nullable=false) private String eventType;
    @Lob @Column(nullable=false) private String payload; // JSON
    @Enumerated(EnumType.STRING) @Column(nullable=false) private Status status;
    @Column(nullable=false) private Instant createdAt;
    private Instant sentAt;
    @Column(length=2000) private String lastError;
    @Column(nullable=false) private int attempts;

    public static OutboxEntity pending(String aggregateId, String eventType, String payload){
        OutboxEntity e = new OutboxEntity();
        e.id = UUID.randomUUID().toString();
        e.aggregateId = aggregateId; e.eventType = eventType; e.payload = payload;
        e.status = Status.PENDING; e.createdAt = Instant.now(); e.attempts = 0;
        return e;
    }
}
