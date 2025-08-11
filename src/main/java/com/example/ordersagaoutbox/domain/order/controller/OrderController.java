package com.example.ordersagaoutbox.domain.order.controller;

import com.example.ordersagaoutbox.domain.inventory.repository.InventoryRepository;
import com.example.ordersagaoutbox.domain.inventory.service.InventoryService;
import com.example.ordersagaoutbox.domain.order.dto.CreateOrderRequest;
import com.example.ordersagaoutbox.domain.order.entity.OrderEntity;
import com.example.ordersagaoutbox.domain.order.repository.OrderRepository;
import com.example.ordersagaoutbox.domain.order.service.OrderService;
import com.example.ordersagaoutbox.domain.saga.SagaOrchestrator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    private final SagaOrchestrator orchestrator;
    private final OrderRepository orderRepo;
    private final InventoryService inventoryService;
    private final InventoryRepository inventoryRepo;

    // 재고 시드
    @PostMapping("/inventory/seed")
    public ResponseEntity<?> seed(@RequestParam String productId, @RequestParam int qty){
        inventoryService.seed(productId, qty);
        return ResponseEntity.ok().build();
    }

    // 주문 생성(멱등키 필수)
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody CreateOrderRequest req,
                                    @RequestHeader("Idempotency-Key") String idemKey){
        OrderEntity o = orchestrator.placeOrder(req, idemKey);
        return ResponseEntity.ok(o);
    }

    // 조회
    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable String id){
        return orderRepo.findById(id).<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // (보너스) 재고 조회
    @GetMapping("/inventory/{productId}")
    public ResponseEntity<?> inv(@PathVariable String productId){
        return inventoryRepo.findById(productId)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

}
