package com.example.storage.service;

import com.example.storage.dto.ItemRequest;
import com.example.storage.dto.ItemResponse;
import com.example.storage.entity.Item;
import com.example.storage.exception.ItemNotFoundException;
import com.example.storage.repository.ItemRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ItemService {

    private final ItemRepository repository;

    public ItemService(ItemRepository repository) {
        this.repository = repository;
    }

    public ItemResponse create(ItemRequest request) {
        Item saved = repository.save(new Item(request.name(), request.description()));
        return ItemResponse.from(saved);
    }

    public List<ItemResponse> findAll() {
        return repository.findAll().stream().map(ItemResponse::from).toList();
    }

    public ItemResponse findById(Long id) {
        return repository.findById(id).map(ItemResponse::from).orElseThrow(() -> new ItemNotFoundException(id));
    }
}
