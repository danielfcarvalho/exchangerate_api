package com.dfc.exchange_api.backend.exceptions;

/**
 * Thrown when the parameter received in REST endpoints for a currency code is invalid, that is, doesn't
 * belong to any of the supported Currencies.
 */
public class InvalidCurrencyException extends RuntimeException {
    public InvalidCurrencyException(String message) {
        super(message);
    }
}
