package com.example.storage.controller;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoints that exist only to demonstrate retry, error propagation and the circuit breaker.
 */
@RestController
@RequestMapping("/storage/test")
public class FailureSimulationController {

    private final AtomicInteger counter = new AtomicInteger();

    /** Fails the first {@code failures} calls, then succeeds and resets the counter. */
    @GetMapping("/flaky")
    public Map<String, Object> flaky(@RequestParam(defaultValue = "2") int failures) {
        int attempt = counter.incrementAndGet();
        if (attempt <= failures) {
            throw new IllegalStateException("Simulated failure " + attempt + " of " + failures);
        }
        counter.set(0);
        return Map.of("message", "Succeeded after " + (attempt - 1) + " simulated failures", "attempt", attempt);
    }

    /** Always fails with HTTP 500. */
    @GetMapping("/error")
    public Map<String, Object> error() {
        throw new IllegalStateException("Simulated storage-service failure");
    }

    /** Responds after the given delay, used to trigger Feign read timeouts. */
    @GetMapping("/slow")
    public Map<String, Object> slow(@RequestParam(defaultValue = "5000") long ms) throws InterruptedException {
        Thread.sleep(ms);
        return Map.of("message", "Responded after " + ms + " ms");
    }

    @PostMapping("/reset")
    public Map<String, String> reset() {
        counter.set(0);
        return Map.of("message", "Flaky counter reset");
    }
}
