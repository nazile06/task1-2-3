package com.example.client.controller;

import com.example.client.dto.ItemRequest;
import com.example.client.dto.ItemResponse;
import com.example.client.service.ItemProcessor;
import com.example.client.service.StorageGateway;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ClientController {

    private final StorageGateway gateway;
    private final ItemProcessor processor;

    public ClientController(StorageGateway gateway, ItemProcessor processor) {
        this.gateway = gateway;
        this.processor = processor;
    }

    @GetMapping("/hello")
    public Map<String, Object> hello() {
        return gateway.hello();
    }

    @PostMapping("/items")
    public ResponseEntity<ItemResponse> create(@Valid @RequestBody ItemRequest request) {
        ItemResponse saved = gateway.create(processor.process(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/items")
    public List<ItemResponse> findAll() {
        return gateway.findAll();
    }

    @GetMapping("/items/{id}")
    public ItemResponse findById(@PathVariable Long id) {
        return gateway.findById(id);
    }
}
