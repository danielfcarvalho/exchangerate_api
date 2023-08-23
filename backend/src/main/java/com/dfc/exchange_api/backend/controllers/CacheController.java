package com.dfc.exchange_api.backend.controllers;

import com.dfc.exchange_api.backend.exceptions.CacheNotFoundException;
import com.dfc.exchange_api.backend.services.CacheService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@Tag(name = "3. Cache Controller", description = "Endpoints to manage and monitor the cache")
@RestController
@RequestMapping("/api/v1/cache")
public class CacheController {
    private static final Logger LOGGER = LoggerFactory.getLogger(CacheController.class);
    private CacheService cacheService;

    public CacheController(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    // CACHE MANAGEMENT ENDPOINTS

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Valid response",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "No cache was found",
                    content = @Content),})
    @Operation(summary = "Get all of the entries stored in the exchangeRate cache")
    @GetMapping("/entries/all")
    public ResponseEntity<Object> getAllCacheEntries() throws CacheNotFoundException {
        LOGGER.info("Received a request on the GET /cache/entries/all endpoint");

        return ResponseEntity.ok().body(cacheService.getAllCacheEntries());
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Valid response",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "No cache was found",
                    content = @Content),})
    @Operation(summary = "Get all of the keys stored in the exchangeRate cache")
    @GetMapping("/entries/keys")
    public ResponseEntity<Object> getAllCacheKeys() throws CacheNotFoundException {
        LOGGER.info("Received a request on the GET /cache/entries/keys endpoint");

        return ResponseEntity.ok().body(cacheService.getAllCacheKeys());
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Valid response",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "No cache was found",
                    content = @Content),})
    @Operation(summary = "Get the value associated with a given key in the exchangeRate cache")
    @GetMapping("/entries/{key}")
    public ResponseEntity<Object> getSingleValue(@PathVariable(name = "key") String key) throws CacheNotFoundException {
        LOGGER.info("Received a request on the GET /cache/entries/{key}} endpoint");

        Object value = cacheService.getSingleValue(key);

        if(value != null){
            return ResponseEntity.ok().body(value);
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("The key wasn't present in the cache");
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Valid response",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "No cache was found",
                    content = @Content),})
    @Operation(summary = "Clear the exchangeRate cache")
    @DeleteMapping("/entries/all")
    public ResponseEntity<Object> deleteAllCacheEntries() throws CacheNotFoundException {
        LOGGER.info("Received a request on the DELETE /cache/entries endpoint");

        cacheService.deleteAllCacheEntries();
        return ResponseEntity.status(HttpStatus.OK).body("Cache cleared successfully");
    }

    // CACHE STATISTICS ENDPOINTS
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Valid response",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "No cache was found",
                    content = @Content),})
    @Operation(summary = "Get exchangeRate cache statistics, like number of hits, misses and evictions")
    @GetMapping("/statistics/all")
    public ResponseEntity<Object> getAllStatistics() throws CacheNotFoundException{
        LOGGER.info("Received a request on the GET /cache/statistics/all endpoint");

        return ResponseEntity.ok().body(cacheService.getAllStatistics());
    }
}
