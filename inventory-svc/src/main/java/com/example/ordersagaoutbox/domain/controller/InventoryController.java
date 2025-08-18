package com.example.ordersagaoutbox.domain.controller;

import com.example.ordersagaoutbox.domain.dto.InventoryResponseDto;
import com.example.ordersagaoutbox.domain.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/inventory")
public class InventoryController {
    private final InventoryService inventoryService;

    @PostMapping("/seed")
    public ResponseEntity<Void> seed(
            @RequestParam("productId") String productId,
            @RequestParam("qty") int qty
    ){
        inventoryService.seed(productId, qty);
        return ResponseEntity.ok().build();
    }
    @GetMapping("/{productId}")
    public ResponseEntity<InventoryResponseDto> get(
            @PathVariable("productId") String productId
    ){
        return ResponseEntity.ok(inventoryService.getInventory(productId));
    }
    @PostMapping("/reserve")
    public ResponseEntity<Void> reserve(@RequestParam("productId") String productId, @RequestParam("qty") int qty){
        inventoryService.reserve(productId, qty); return ResponseEntity.ok().build();
    }
    @PostMapping("/release")
    public ResponseEntity<Void> release(@RequestParam("productId") String productId, @RequestParam("qty") int qty){
        inventoryService.release(productId, qty); return ResponseEntity.ok().build();
    }
}
