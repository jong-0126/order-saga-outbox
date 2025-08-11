package com.example.ordersagaoutbox.domain.inventory.service;

import com.example.ordersagaoutbox.domain.inventory.entity.InventoryEntity;
import com.example.ordersagaoutbox.domain.inventory.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InventoryService {
    private final InventoryRepository inventoryRepository;

    @Transactional
    public void seed(String productId, int qty){
        InventoryEntity inv = inventoryRepository.findById(productId).orElseGet(() -> {
            InventoryEntity i = new InventoryEntity(); i.setProductId(productId); i.setAvailableQty(0); return i;
        });
        inv.setAvailableQty(qty);
        inventoryRepository.save(inv);
    }

    @Transactional
    public void reserve(String productId, int qty){
        InventoryEntity inv = inventoryRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Inventory not found: " + productId));
        if (inv.getAvailableQty() < qty) throw new IllegalStateException("Not enough stock");
        inv.setAvailableQty(inv.getAvailableQty() - qty);
    }

    @Transactional
    public void release(String productId, int qty){ // 보상
        InventoryEntity inv = inventoryRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Inventory not found: " + productId));
        inv.setAvailableQty(inv.getAvailableQty() + qty);
    }
}
