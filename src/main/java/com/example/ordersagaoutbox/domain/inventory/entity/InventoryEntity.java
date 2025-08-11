package com.example.ordersagaoutbox.domain.inventory.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

@Entity
@Table(name = "inventory")
@Getter
public class InventoryEntity {
    @Id
    private String productId;              // 제품 ID(=PK)
    @Column(nullable=false)
    private int availableQty;              // 남은 수량
}
