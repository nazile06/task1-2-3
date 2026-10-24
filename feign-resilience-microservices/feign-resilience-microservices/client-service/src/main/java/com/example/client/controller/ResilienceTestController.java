package com.example.client.controller;

import com.example.client.service.StorageGateway;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

/** Endpoints used to demonstrate retry, error propagation and the circuit breaker. */
@RestController
@RequestMapping("/api")
public class ResilienceTestController {

    private final StorageGateway gateway;
    private final CircuitBreakerRegistry registry;

    public ResilienceTestController(StorageGateway gateway, CircuitBreakerRegistry registry) {
        this.gateway = gateway;
        this.registry = registry;
    }

    @GetMapping("/test/flaky")
    public Map<String, Object> flaky(@RequestParam(defaultValue = "2") int failures) {
        return gateway.flaky(failures);
    }

    @GetMapping("/test/error")
    public Map<String, Object> error() {
        return gateway.error();
    }

    @GetMapping("/test/slow")
    public Map<String, Object> slow(@RequestParam(defaultValue = "5000") long ms) {
        return gateway.slow(ms);
    }

    @PostMapping("/test/reset")
    public Map<String, Object> reset() {
        return gateway.reset();
    }

    @GetMapping("/circuit-breaker/state")
    public Map<String, Object> circuitBreakerState() {
        CircuitBreaker cb = registry.circuitBreaker("storage");
        CircuitBreaker.Metrics m = cb.getMetrics();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("name", cb.getName());
        body.put("state", cb.getState().name());
        body.put("failureRate", m.getFailureRate());
        body.put("bufferedCalls", m.getNumberOfBufferedCalls());
        body.put("failedCalls", m.getNumberOfFailedCalls());
        body.put("successfulCalls", m.getNumberOfSuccessfulCalls());
        body.put("notPermittedCalls", m.getNumberOfNotPermittedCalls());
        return body;
    }
}
