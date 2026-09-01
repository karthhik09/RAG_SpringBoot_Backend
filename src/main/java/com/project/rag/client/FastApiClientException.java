package com.project.rag.client;

public class FastApiClientException extends RuntimeException {

    public FastApiClientException(String message) {
        super(message);
    }

    public FastApiClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
