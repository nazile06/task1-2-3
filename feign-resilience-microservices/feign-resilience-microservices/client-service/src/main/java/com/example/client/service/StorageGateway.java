package com.example.client.service;

import com.example.client.client.StorageClient;
import com.example.client.dto.ItemRequest;
import com.example.client.dto.ItemResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Single entry point for all calls to storage-service.
 * Every call is protected by Retry (outer) and CircuitBreaker (inner), see application.yml.
 * The log line in each method is printed once per attempt, which makes retries visible.
 */
@Service
@Retry(name = "storage")
@CircuitBreaker(name = "storage")
public class StorageGateway {

    private static final Logger log = LoggerFactory.getLogger(StorageGateway.class);

    private final StorageClient client;

    public StorageGateway(StorageClient client) {
        this.client = client;
    }

    public Map<String, Object> hello() {
        log.info("Calling storage-service: GET /storage/hello");
        return client.hello();
    }

    public ItemResponse create(ItemRequest request) {
        log.info("Calling storage-service: POST /storage/items");
        return client.create(request);
    }

    public List<ItemResponse> findAll() {
        log.info("Calling storage-service: GET /storage/items");
        return client.findAll();
    }

    public ItemResponse findById(Long id) {
        log.info("Calling storage-service: GET /storage/items/{}", id);
        return client.findById(id);
    }

    public Map<String, Object> flaky(int failures) {
        log.info("Calling storage-service: GET /storage/test/flaky?failures={}", failures);
        return client.flaky(failures);
    }

    public Map<String, Object> error() {
        log.info("Calling storage-service: GET /storage/test/error");
        return client.error();
    }

    public Map<String, Object> slow(long ms) {
        log.info("Calling storage-service: GET /storage/test/slow?ms={}", ms);
        return client.slow(ms);
    }

    public Map<String, Object> reset() {
        log.info("Calling storage-service: POST /storage/test/reset");
        return client.reset();
    }
}
