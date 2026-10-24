package com.example.client.dto;

import java.time.Instant;
import org.springframework.http.HttpStatus;

public record ApiError(Instant timestamp, int status, String error, String message, String path, String source) {

    public static ApiError of(int status, String message, String path, String source) {
        HttpStatus resolved = HttpStatus.resolve(status);
        String reason = resolved != null ? resolved.getReasonPhrase() : "Error";
        return new ApiError(Instant.now(), status, reason, message, path, source);
    }
}
