package com.example.ordersagaoutbox.domain.inventory.repository;

import com.example.ordersagaoutbox.domain.inventory.entity.InventoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryRepository extends JpaRepository<InventoryEntity, String> {
}
