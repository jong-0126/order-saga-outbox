package com.example.ordersagaoutbox.domain.controller;

import com.example.ordersagaoutbox.domain.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/inventory")
public class InventoryController {
    private final InventoryService service;

    // 재고 시드 (테스트용)
    @PostMapping("/seed")
    public ResponseEntity<Void> seed(@RequestParam String productId, @RequestParam int qty) {
        service.seed(productId, qty);
        return ResponseEntity.ok().build();
    }

    // 수량 조회
    @GetMapping("/{productId}")
    public Map<String, Object> get(@PathVariable String productId) {
        return Map.of("productId", productId, "qty", service.getQty(productId));
    }

    // 수동 예약/해제(테스트 편의용)
    @PostMapping("/reserve")
    public ResponseEntity<Void> reserve(@RequestParam String productId, @RequestParam int qty) {
        service.reserve(productId, qty);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/release")
    public ResponseEntity<Void> release(@RequestParam String productId, @RequestParam int qty) {
        service.release(productId, qty);
        return ResponseEntity.ok().build();
    }
}
