package com.example.ordersagaoutbox.domain.outbox.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "payment_outbox")
@Getter @Setter @Builder @AllArgsConstructor @NoArgsConstructor
public class OutboxEntity {
    public enum Status { PENDING, SENT, FAILED }

    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    private String aggregateId;     // orderId 등 상관키
    private String type;            // EventTypes 값
    @Lob @Column(columnDefinition="TEXT")
    private String payload;         // 이벤트 JSON 문자열

    @Enumerated(EnumType.STRING)
    private Status status;          // 현재 상태

    private int attempts;           // 전송 시도 횟수
    private String lastError;       // 마지막 오류 메세지
    private Instant createdAt;      // 생성 시각
    private Instant sentAt;         // 성공 발행 시각

    @PrePersist                     // INSERT 직전 호출
    public void pre(){
        if(createdAt==null) createdAt=Instant.now();    // 생성시각
        if(status==null) status=Status.PENDING;         // 기본 상태는 PENDING
    }
}
