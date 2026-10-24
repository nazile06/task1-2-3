package com.example.client.exception;

import com.example.client.dto.ApiError;
import feign.RetryableException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String CLIENT = "client-service";
    private static final String STORAGE = "storage-service";

    /** Error propagation: the original status and message from storage-service are passed through. */
    @ExceptionHandler(DownstreamException.class)
    public ResponseEntity<ApiError> downstream(DownstreamException ex, HttpServletRequest req) {
        return ResponseEntity.status(ex.getStatus())
                .body(ApiError.of(ex.getStatus(), ex.getMessage(), req.getRequestURI(), STORAGE));
    }

    /** Circuit is OPEN: the call was rejected immediately without contacting storage-service. */
    @ExceptionHandler(CallNotPermittedException.class)
    public ResponseEntity<ApiError> circuitOpen(CallNotPermittedException ex, HttpServletRequest req) {
        return build(HttpStatus.SERVICE_UNAVAILABLE,
                "Circuit breaker is OPEN: calls to storage-service are temporarily blocked", req);
    }

    /** Connection refused or timeout, after all retry attempts were used. */
    @ExceptionHandler(RetryableException.class)
    public ResponseEntity<ApiError> unreachable(RetryableException ex, HttpServletRequest req) {
        log.warn("storage-service unreachable: {}", ex.getMessage());
        return build(HttpStatus.SERVICE_UNAVAILABLE,
                "storage-service is unreachable or timed out after retries", req);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> validation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return build(HttpStatus.BAD_REQUEST, message, req);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> unexpected(Exception ex, HttpServletRequest req) {
        log.error("Unexpected error", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), req);
    }

    private ResponseEntity<ApiError> build(HttpStatus status, String message, HttpServletRequest req) {
        return ResponseEntity.status(status)
                .body(ApiError.of(status.value(), message, req.getRequestURI(), CLIENT));
    }
}
