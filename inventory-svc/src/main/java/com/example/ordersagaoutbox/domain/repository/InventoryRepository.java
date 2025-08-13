package com.example.ordersagaoutbox.domain.repository;

import com.example.ordersagaoutbox.domain.entity.InventoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryRepository extends JpaRepository<InventoryEntity, String> {
}
