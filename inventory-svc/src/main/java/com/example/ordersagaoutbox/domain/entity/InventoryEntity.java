package com.example.ordersagaoutbox.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
import lombok.*;

import java.time.Instant;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class InventoryEntity {
    @Id
    private String productId;
    private int quantity;
    private Instant updatedAt;

    @Version
    private Long version;
}
