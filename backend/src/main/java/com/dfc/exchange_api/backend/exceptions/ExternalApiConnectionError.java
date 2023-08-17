package com.dfc.exchange_api.backend.exceptions;

/**
 * Exception thrown when the External API timeouts, or returns antyhing but a valid response with Status OK
 */
public class ExternalApiConnectionError extends RuntimeException {
    public ExternalApiConnectionError(String message) {
        super(message);
    }
}
