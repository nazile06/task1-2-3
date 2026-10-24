package com.example.client.exception;

/** storage-service answered with 4xx. Not retried and not counted by the circuit breaker. */
public class DownstreamClientException extends DownstreamException {
    public DownstreamClientException(int status, String message) {
        super(status, message);
    }
}
