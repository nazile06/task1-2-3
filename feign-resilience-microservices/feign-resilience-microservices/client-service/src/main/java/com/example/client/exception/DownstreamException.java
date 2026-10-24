package com.example.client.exception;

/** An error response received from storage-service, carrying its original status and message. */
public abstract class DownstreamException extends RuntimeException {

    private final int status;

    protected DownstreamException(int status, String message) {
        super(message);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
