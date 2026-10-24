package com.example.client.service;

import com.example.client.dto.ItemRequest;
import org.springframework.stereotype.Component;

/** Processing step applied before data is passed to storage-service. */
@Component
public class ItemProcessor {

    public ItemRequest process(ItemRequest request) {
        String name = request.name().trim().replaceAll("\\s+", " ").toUpperCase();
        String description = request.description() == null || request.description().isBlank()
                ? "N/A"
                : request.description().trim();
        return new ItemRequest(name, description);
    }
}
