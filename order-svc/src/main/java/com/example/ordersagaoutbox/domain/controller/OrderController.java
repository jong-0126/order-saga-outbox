package com.example.ordersagaoutbox.domain.controller;

import com.example.ordersagaoutbox.domain.dto.CreateOrderRequest;
import com.example.ordersagaoutbox.domain.entity.OrderEntity;
import com.example.ordersagaoutbox.domain.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderEntity> create(
            @RequestBody @Valid CreateOrderRequest req,
            @RequestHeader(value = "Idempotency-Key", required = false) String idemKey
    ) {
        String key = (idemKey != null && !idemKey.isBlank()) ? idemKey : UUID.randomUUID().toString();
        OrderEntity order = orderService.createIfAbsent(req, key);
        return ResponseEntity.ok(order);
    }
}
