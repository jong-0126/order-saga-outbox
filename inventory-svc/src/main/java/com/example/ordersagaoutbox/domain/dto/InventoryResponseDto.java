package com.example.ordersagaoutbox.domain.dto;

import lombok.Getter;

import java.time.Instant;

@Getter
public class InventoryResponseDto {

    private final String productId;
    private final int quantity;
    private final Instant createdAt;
    private final Instant updatedAt;

    public InventoryResponseDto(String productId, int quantity, Instant createdAt, Instant updatedAt) {
        this.productId = productId;
        this.quantity = quantity;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
