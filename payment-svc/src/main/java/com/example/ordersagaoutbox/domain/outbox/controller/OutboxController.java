package com.example.ordersagaoutbox.domain.outbox.controller;

import com.example.ordersagaoutbox.domain.outbox.entity.OutboxEntity;
import com.example.ordersagaoutbox.domain.outbox.repository.OutboxRepository;
import com.example.ordersagaoutbox.domain.outbox.service.OutboxService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/outbox")
public class OutboxController {

    private final OutboxRepository repo;
    private final OutboxService service;

    public OutboxController(OutboxRepository repo, OutboxService service){
        this.repo = repo; this.service = service;
    }

    @GetMapping("/pending")
    public List<OutboxEntity> pending(){
        return repo.findTop100ByStatusOrderByCreatedAtAsc(OutboxEntity.Status.PENDING);
    }

    @PostMapping("/publish")
    public ResponseEntity<?> publishNow(){
        service.publishOnce();
        return ResponseEntity.ok().build();
    }
}
