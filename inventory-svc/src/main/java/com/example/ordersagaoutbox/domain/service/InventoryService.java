package com.example.ordersagaoutbox.domain.service;

import com.example.ordersagaoutbox.domain.entity.InventoryEntity;
import com.example.ordersagaoutbox.domain.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class InventoryService {
    private final InventoryRepository repo;

    @Transactional
    public void seed(String productId, int qty) {
        InventoryEntity e = repo.findById(productId)
                .orElseGet(() -> InventoryEntity.builder().productId(productId).quantity(0).build());
        e.setQuantity(qty);
        e.setUpdatedAt(Instant.now());
        repo.save(e);
    }

    @Transactional(readOnly = true)
    public int getQty(String productId) {
        return repo.findById(productId).map(InventoryEntity::getQuantity).orElse(0);
    }

    @Transactional
    public void reserve(String productId, int qty) {
        InventoryEntity e = repo.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("product not found: " + productId));
        if (e.getQuantity() < qty) throw new IllegalStateException("not enough stock");
        e.setQuantity(e.getQuantity() - qty);
        e.setUpdatedAt(Instant.now());
    }

    @Transactional
    public void release(String productId, int qty) {
        InventoryEntity e = repo.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("product not found: " + productId));
        e.setQuantity(e.getQuantity() + qty);
        e.setUpdatedAt(Instant.now());
    }
}
