package com.example.client.client;

import com.example.client.dto.ItemRequest;
import com.example.client.dto.ItemResponse;
import java.util.List;
import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "storage-service", url = "${storage.service.url}")
public interface StorageClient {

    @GetMapping("/storage/hello")
    Map<String, Object> hello();

    @PostMapping("/storage/items")
    ItemResponse create(@RequestBody ItemRequest request);

    @GetMapping("/storage/items")
    List<ItemResponse> findAll();

    @GetMapping("/storage/items/{id}")
    ItemResponse findById(@PathVariable("id") Long id);

    @GetMapping("/storage/test/flaky")
    Map<String, Object> flaky(@RequestParam("failures") int failures);

    @GetMapping("/storage/test/error")
    Map<String, Object> error();

    @GetMapping("/storage/test/slow")
    Map<String, Object> slow(@RequestParam("ms") long ms);

    @PostMapping("/storage/test/reset")
    Map<String, Object> reset();
}
