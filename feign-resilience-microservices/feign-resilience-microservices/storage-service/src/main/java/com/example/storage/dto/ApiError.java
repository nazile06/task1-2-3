package com.example.storage.dto;

import java.time.Instant;
import org.springframework.http.HttpStatus;

public record ApiError(Instant timestamp, int status, String error, String message, String path, String source) {

    public static ApiError of(HttpStatus status, String message, String path) {
        return new ApiError(Instant.now(), status.value(), status.getReasonPhrase(), message, path, "storage-service");
    }
}
