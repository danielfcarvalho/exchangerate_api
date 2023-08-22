package com.dfc.exchange_api.backend.controllers;

import com.dfc.exchange_api.backend.exceptions.CacheNotFoundException;
import com.dfc.exchange_api.backend.services.CacheService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@Tag(name = "Cache Controller", description = "Endpoints to manage and monitor the cache")
@RestController
@RequestMapping("/api/v1/cache")
public class CacheController {
    private static final Logger LOGGER = LoggerFactory.getLogger(CacheController.class);
    private CacheService cacheService;

    public CacheController(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    // CACHE MANAGEMENT ENDPOINTS

    @GetMapping("/entries/all")
    public ResponseEntity<Object> getAllCacheEntries() throws CacheNotFoundException {
        LOGGER.info("Received a request on the GET /cache/entries/all endpoint");

        return ResponseEntity.ok().body(cacheService.getAllCacheEntries());
    }

    @GetMapping("/entries/keys")
    public ResponseEntity<Object> getAllCacheKeys() throws CacheNotFoundException {
        LOGGER.info("Received a request on the GET /cache/entries/keys endpoint");

        return ResponseEntity.ok().body(cacheService.getAllCacheKeys());
    }

    @GetMapping("/entries/{key}")
    public ResponseEntity<Object> getSingleValue(@PathVariable(name = "key") String key) throws CacheNotFoundException {
        LOGGER.info("Received a request on the GET /cache/entries/{key}} endpoint");

        Object value = cacheService.getSingleValue(key);

        if(value != null){
            return ResponseEntity.ok().body(value);
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("The key wasn't present in the cache");
    }

    @DeleteMapping("/entries/all")
    public ResponseEntity<Object> deleteAllCacheEntries() throws CacheNotFoundException {
        LOGGER.info("Received a request on the DELETE /cache/entries endpoint");

        cacheService.deleteAllCacheEntries();
        return ResponseEntity.status(HttpStatus.OK).body("Cache cleared succesfully");
    }

    // CACHE STATISTICS ENDPOINTS
    @GetMapping("/statistics/all")
    public ResponseEntity<Object> getAllStatistics() throws CacheNotFoundException{
        LOGGER.info("Received a request on the /cache/statistics/all endpoint");

        return ResponseEntity.ok().body(cacheService.getAllStatistics());
    }
}
