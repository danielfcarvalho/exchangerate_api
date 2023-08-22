package com.dfc.exchange_api.backend.services;

import com.dfc.exchange_api.backend.controllers.CacheController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

@Service
public class CacheService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CacheService.class);
    private CacheManager cacheManager;

    public CacheService(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    // CACHE MANAGEMENT ENDPOINTS
    public ResponseEntity<Object> getAllCacheEntries() {
        

        return null;
    }

    public ResponseEntity<Object> getAllCacheKeys() {
        LOGGER.info("Received a request on the /cache/entries/keys endpoint");

        return null;
    }

    public ResponseEntity<Object> getSingleValue(@PathVariable(name = "key") String key) {
        LOGGER.info("Received a request on the GET /cache/entries/{key}} endpoint");

        return null;
    }

    public ResponseEntity<Object> getCacheDetails() {
        LOGGER.info("Received a request on the /cache/details endpoint");

        return null;
    }

    public ResponseEntity<Object> deleteAllCacheEntries() {
        LOGGER.info("Received a request on the DELETE /cache/entries/all endpoint");

        return null;
    }

    public ResponseEntity<Object> deleteSingleValue(@PathVariable(name = "key") String key) {
        LOGGER.info("Received a request on the DELETE /cache/entries/{key}} endpoint");

        return null;
    }

    public ResponseEntity<Object> updateCache() {
        LOGGER.info("Received a request on the PATCH cache/entries/all endpoint");

        return null;
    }

    public ResponseEntity<Object> updateTTL() {
        LOGGER.info("Received a request on the /cache/details/ttl endpoint");

        return null;
    }

    // CACHE STATISTICS ENDPOINTS
    public ResponseEntity<Object> getAllStatistics() {
        LOGGER.info("Received a request on the /cache/statistics/all endpoint");

        return null;
    }

    public ResponseEntity<Object> getCacheHits() {
        LOGGER.info("Received a request on the /cache/statistics/hits endpoint");

        return null;
    }

    public ResponseEntity<Object> getCacheMisses() {
        LOGGER.info("Received a request on the /cache/statistics/misses endpoint");

        return null;
    }

    public ResponseEntity<Object> getCacheSize() {
        LOGGER.info("Received a request on the /cache/statistics/size endpoint");

        return null;
    }
}
