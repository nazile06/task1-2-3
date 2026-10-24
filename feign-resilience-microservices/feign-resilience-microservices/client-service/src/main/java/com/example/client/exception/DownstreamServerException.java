package com.example.client.exception;

/** storage-service answered with 5xx. Retried and counted as a failure by the circuit breaker. */
public class DownstreamServerException extends DownstreamException {
    public DownstreamServerException(int status, String message) {
        super(status, message);
    }
}
