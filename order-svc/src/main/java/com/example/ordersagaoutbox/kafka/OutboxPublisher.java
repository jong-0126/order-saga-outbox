package com.example.ordersagaoutbox.kafka;

import com.example.ordersagaoutbox.domain.outbox.entity.OutboxEntity;
import com.example.ordersagaoutbox.domain.outbox.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class OutboxPublisher {
    private final OutboxRepository repo;
    private final KafkaTemplate<String, String> kafka;
    @Value("${app.kafka.topic:saga.events}") String topic;

    @Scheduled(fixedDelayString="${app.outbox.publish-interval-ms:1000}")
    @Transactional
    public void publishOnce() {
        var list = repo.findTop100ByStatusOrderByCreatedAtAsc(OutboxEntity.Status.PENDING);
        for (var e : list) {
            try {
                kafka.send(topic, e.getAggregateId(), e.getPayload()).get();
                e.setStatus(OutboxEntity.Status.SENT); e.setSentAt(Instant.now());
            } catch (Exception ex) {
                e.setStatus(OutboxEntity.Status.FAILED);
                e.setAttempts(e.getAttempts()+1); e.setLastError(ex.getMessage());
            }
        }
    }
}
