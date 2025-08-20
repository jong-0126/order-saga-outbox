package com.example.ordersagaoutbox.domain.outbox.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "order_outbox")
@Getter @Setter @Builder @AllArgsConstructor @NoArgsConstructor
public class OutboxEntity {
    public enum Status { PENDING, SENT, FAILED }

    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    private String aggregateId;     // orderId 등 상관키
    private String type;            // EventTypes 값
    @Lob @Column(columnDefinition="TEXT")
    private String payload;         // JSON

    @Enumerated(EnumType.STRING)
    private Status status;

    private int attempts;
    private String lastError;
    private Instant createdAt;
    private Instant sentAt;

    @PrePersist public void pre(){ if(createdAt==null) createdAt=Instant.now(); if(status==null) status=Status.PENDING; }

}
