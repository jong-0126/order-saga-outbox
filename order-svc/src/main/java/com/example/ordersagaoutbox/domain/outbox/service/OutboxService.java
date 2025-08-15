package com.example.ordersagaoutbox.domain.outbox.service;

import com.example.ordersagaoutbox.domain.outbox.entity.OutboxEntity;
import com.example.ordersagaoutbox.domain.outbox.repository.OutboxRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class OutboxService {
    private static final Logger log = LoggerFactory.getLogger(OutboxService.class);

    private final OutboxRepository repo;
    private final ObjectMapper om;
    private final int batchSize;

    public OutboxService(OutboxRepository repo, ObjectMapper om,
                         @Value("${app.outbox.batch-size:100}") int batchSize) {
        this.repo = repo; this.om = om; this.batchSize = batchSize;
    }

    /** 상태 변경 등 비즈니스 트랜잭션 내부에서 호출: 같은 트랜잭션으로 기록 */
    @Transactional(propagation = Propagation.MANDATORY)
    public void append(String aggregateId, String eventType, Object envelope) {
        try {
            String json = om.writeValueAsString(envelope);
            repo.save(OutboxEntity.pending(aggregateId, eventType, json));
        } catch (Exception e) {
            throw new RuntimeException("Outbox serialize failed", e);
        }
    }

    /** 폴링 퍼블리셔: PENDING → (로그 발행) → SENT */
    @Scheduled(fixedDelayString = "${app.outbox.publish-interval-ms:1000}")
    @Transactional
    public void publishPending() {
        List<OutboxEntity> list = repo.findTop100ByStatusOrderByCreatedAtAsc(OutboxEntity.Status.PENDING);
        for (OutboxEntity e : list) {
            try {
                // 여기서는 '로그 발행'으로 대체. (Step 4에서 Kafka로 교체)
                log.info("PUBLISH eventType={} key={} payload={}", e.getEventType(), e.getAggregateId(), e.getPayload());
                e.setStatus(OutboxEntity.Status.SENT);
                e.setSentAt(Instant.now());
            } catch (Exception ex) {
                e.setStatus(OutboxEntity.Status.FAILED);
                e.setAttempts(e.getAttempts()+1);
                e.setLastError(ex.getMessage());
                log.error("Outbox publish failed id={}", e.getId(), ex);
            }
        }
    }

    /** 수동 트리거(디버그용) */
    @Transactional public int publishOnce() { publishPending(); return 0; }
}
