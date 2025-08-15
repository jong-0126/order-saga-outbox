package com.example.ordersagaoutbox.domain.outbox.repository;

import com.example.ordersagaoutbox.domain.outbox.entity.OutboxEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OutboxRepository extends JpaRepository<OutboxEntity, String> {
    List<OutboxEntity> findTop100ByStatusOrderByCreatedAtAsc(OutboxEntity.Status status);
}
