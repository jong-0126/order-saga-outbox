package com.example.ordersagaoutbox.infra.kafka;

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
    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${app.kafka.topic:saga.events}")
    String topic;   // 토픽명(기본 saga.events)

    @Scheduled(fixedDelayString="${app.outbox.publish-interval-ms:1000}")
    @Transactional      // 배치 단위 트랜잭션
    public void publishOnce() {
        var list = outboxRepository.findTop100ByStatusOrderByCreatedAtAsc(OutboxEntity.Status.PENDING);
        // PENDING 100건까지 오래된 순으로 조회

        for (var e : list) {
            try {
                kafkaTemplate.send(topic, e.getAggregateId(), e.getPayload()).get();
                // key = aggregateId, value = payload(JSON) 전송. get() 으로 동기 확인

                e.setStatus(OutboxEntity.Status.SENT); // 성공 처리
                e.setSentAt(Instant.now());
            } catch (Exception ex) {
                e.setStatus(OutboxEntity.Status.FAILED); // 실패 시 상태 / 시도 수 업데이트
                e.setAttempts(e.getAttempts()+1);
                e.setLastError(ex.getMessage());
            }
        }
    }
}
