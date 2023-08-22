package com.dfc.exchange_api.backend.controllers;

import com.dfc.exchange_api.backend.exceptions.ExternalApiConnectionError;
import com.dfc.exchange_api.backend.exceptions.InvalidCurrencyException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.PositiveOrZero;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Cache Controller", description = "Endpoints to manage and monitor the cache")
@RestController
@RequestMapping("/api/v1/cache")
public class CacheController {
    private static final Logger LOGGER = LoggerFactory.getLogger(CacheController.class);

    // CACHE MANAGEMENT ENDPOINTS

    @GetMapping("/entries/all")
    public ResponseEntity<Object> getAllCacheEntries() {
        LOGGER.info("Received a request on the GET /cache/entries/all endpoint");

        return null;
    }

    @GetMapping("/entries/keys")
    public ResponseEntity<Object> getAllCacheKeys() {
        LOGGER.info("Received a request on the /cache/entries/keys endpoint");

        return null;
    }

    @GetMapping("/entries/{key}")
    public ResponseEntity<Object> getSingleValue(@PathVariable(name = "key") String key) {
        LOGGER.info("Received a request on the GET /cache/entries/{key}} endpoint");

        return null;
    }

    @GetMapping("/details")
    public ResponseEntity<Object> getCacheDetails() {
        LOGGER.info("Received a request on the /cache/details endpoint");

        return null;
    }

    @DeleteMapping("/entries/all")
    public ResponseEntity<Object> deleteAllCacheEntries() {
        LOGGER.info("Received a request on the DELETE /cache/entries/all endpoint");

        return null;
    }

    @DeleteMapping("/entries/{key}")
    public ResponseEntity<Object> deleteSingleValue(@PathVariable(name = "key") String key) {
        LOGGER.info("Received a request on the DELETE /cache/entries/{key}} endpoint");

        return null;
    }

    @PatchMapping ("/entries/all")
    public ResponseEntity<Object> updateCache() {
        LOGGER.info("Received a request on the PATCH cache/entries/all endpoint");

        return null;
    }

    @PutMapping("/details/ttl")
    public ResponseEntity<Object> updateTTL() {
        LOGGER.info("Received a request on the /cache/details/ttl endpoint");

        return null;
    }

    // CACHE STATISTICS ENDPOINTS
    @GetMapping("/statistics/all")
    public ResponseEntity<Object> getAllStatistics() {
        LOGGER.info("Received a request on the /cache/statistics/all endpoint");

        return null;
    }

    @GetMapping("/statistics/hits")
    public ResponseEntity<Object> getCacheHits() {
        LOGGER.info("Received a request on the /cache/statistics/hits endpoint");

        return null;
    }

    @GetMapping("/statistics/misses")
    public ResponseEntity<Object> getCacheMisses() {
        LOGGER.info("Received a request on the /cache/statistics/misses endpoint");

        return null;
    }

    @GetMapping("/statistics/size")
    public ResponseEntity<Object> getCacheSize() {
        LOGGER.info("Received a request on the /cache/statistics/size endpoint");

        return null;
    }
}
