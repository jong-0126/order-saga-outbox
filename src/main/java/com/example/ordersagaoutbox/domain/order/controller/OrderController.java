package com.example.ordersagaoutbox.domain.order.controller;

import com.example.ordersagaoutbox.domain.order.dto.CreateOrderRequest;
import com.example.ordersagaoutbox.domain.order.repository.OrderRepository;
import com.example.ordersagaoutbox.domain.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
    private final OrderRepository orderRepository;

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody CreateOrderRequest req){
        return ResponseEntity.ok(orderService.create(req));
    }
    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable String id){
        return orderRepository.findById(id).<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

}
