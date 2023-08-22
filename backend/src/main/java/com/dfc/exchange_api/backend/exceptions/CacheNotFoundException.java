package com.dfc.exchange_api.backend.exceptions;

/**
 * Exception thrown when the Cache is not found
 */
public class CacheNotFoundException extends RuntimeException{
    public CacheNotFoundException(String message) {
        super(message);
    }
}
