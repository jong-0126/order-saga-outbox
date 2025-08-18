package com.example.common.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class EventEnvelope<T> {             // 이벤트 '봉투' (제네릭 T = payload 타입)
    private String type;                    // 이벤트 이름
    private String correlationId;           // 상관키(흐름 추적용)
    private T payload;                      // 실제 데이터(타입 안정)
    private Instant createdAt;              // 생성 시각

    public static <T> EventEnvelope<T> of(String type, String corr, T payload) {
        return new EventEnvelope<>(type, corr, payload, Instant.now());
    }
}
