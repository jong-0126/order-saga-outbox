package com.example.ordersagaoutbox.domain.outbox.service;

import com.example.ordersagaoutbox.domain.outbox.entity.OutboxEntity;
import com.example.ordersagaoutbox.domain.outbox.repository.OutboxRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OutboxService {
    private static final Logger log = LoggerFactory.getLogger(OutboxService.class);

    private final OutboxRepository repo;
    private final ObjectMapper om;

    /** 상태 변경 등 비즈니스 트랜잭션 내부에서 호출: 같은 트랜잭션으로 기록 */
    @Transactional
    public <T> void append(String aggregateId, String type, T envelope) {
        try {
            String json = (envelope instanceof String s) ? s : om.writeValueAsString(envelope);
            var e = OutboxEntity.builder()
                    .aggregateId(aggregateId).type(type).payload(json)
                    .status(OutboxEntity.Status.PENDING).attempts(0).build();
            repo.save(e);
        } catch (Exception e) {
            throw new RuntimeException("Outbox serialize failed", e);
        }
    }
}
