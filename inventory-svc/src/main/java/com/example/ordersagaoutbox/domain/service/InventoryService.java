package com.example.ordersagaoutbox.domain.service;

import com.example.ordersagaoutbox.domain.dto.InventoryResponseDto;
import com.example.ordersagaoutbox.domain.entity.InventoryEntity;
import com.example.ordersagaoutbox.domain.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class InventoryService {
    private final InventoryRepository inventoryRepository;

    @Transactional
    public void seed(String productId, int qty){
        InventoryEntity inventoryEntity = inventoryRepository.findById(productId).orElse(null);

        // 새로운 제품 등록할 때
        if(inventoryEntity == null){
            inventoryEntity = InventoryEntity.builder()
                    .productId(productId)
                    .quantity(qty)
                    .createdAt(Instant.now())
                    .build();
            inventoryRepository.save(inventoryEntity);
        // 기존에 있던 제품 등록할 때
        }else{
            inventoryEntity.setQuantity(inventoryEntity.getQuantity() + qty);
            inventoryEntity.setUpdatedAt(Instant.now());
        }
        inventoryRepository.save(inventoryEntity);
    }

    @Transactional(readOnly = true)
    public InventoryResponseDto getInventory(String productId){
        InventoryEntity inventoryEntity = inventoryRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Dose not exist = " + productId
                ));
        return new InventoryResponseDto(
                inventoryEntity.getProductId(),
                inventoryEntity.getQuantity(),
                inventoryEntity.getCreatedAt(),
                inventoryEntity.getUpdatedAt()
        );
    }

    @Transactional
    public void reserve(String productId, int qty){
        var e = inventoryRepository.findById(productId).orElseThrow(() -> new IllegalArgumentException("product not found: "+productId));
        if (e.getQuantity() < qty) throw new IllegalStateException("not enough stock");
        e.setQuantity(e.getQuantity() - qty); e.setUpdatedAt(Instant.now());
    }

    @Transactional
    public void release(String productId, int qty){
        var e = inventoryRepository.findById(productId).orElseThrow(() -> new IllegalArgumentException("product not found: "+productId));
        e.setQuantity(e.getQuantity() + qty); e.setUpdatedAt(Instant.now());
    }
}
