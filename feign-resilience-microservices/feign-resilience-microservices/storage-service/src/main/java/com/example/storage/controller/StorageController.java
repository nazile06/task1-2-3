package com.example.storage.controller;

import com.example.storage.dto.ItemRequest;
import com.example.storage.dto.ItemResponse;
import com.example.storage.service.ItemService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/storage")
public class StorageController {

    private final ItemService service;

    public StorageController(ItemService service) {
        this.service = service;
    }

    @GetMapping("/hello")
    public Map<String, String> hello() {
        return Map.of("service", "storage-service", "message", "Hello from storage-service");
    }

    @PostMapping("/items")
    public ResponseEntity<ItemResponse> create(@Valid @RequestBody ItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @GetMapping("/items")
    public List<ItemResponse> findAll() {
        return service.findAll();
    }

    @GetMapping("/items/{id}")
    public ItemResponse findById(@PathVariable Long id) {
        return service.findById(id);
    }
}
