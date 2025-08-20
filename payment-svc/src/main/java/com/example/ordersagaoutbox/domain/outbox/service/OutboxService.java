package com.example.ordersagaoutbox.domain.outbox.service;

import com.example.ordersagaoutbox.domain.outbox.entity.OutboxEntity;
import com.example.ordersagaoutbox.domain.outbox.repository.OutboxRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OutboxService {
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;          // 객체 -> JSON 직렬화

    /** 상태 변경 등 비즈니스 트랜잭션 내부에서 호출: 같은 트랜잭션으로 기록 */
    @Transactional
    public <T> void append(String aggregateId, String type, T envelope) {
        try {
            // envelope가 문자열이면 그대로, 객체면 JSON 으로 변환
            String json = (envelope instanceof String s) ? s : objectMapper.writeValueAsString(envelope);
            var e = OutboxEntity.builder()
                    .aggregateId(aggregateId).type(type).payload(json)
                    .status(OutboxEntity.Status.PENDING).attempts(0).build();
            // PENDING 상태로 한 줄 적재
            outboxRepository.save(e);           // 같은 트랜잭션으로 INSERT
        } catch (Exception e) {
            throw new RuntimeException("Outbox serialize failed", e);   // 직렬화 실패 등
        }
    }
}
