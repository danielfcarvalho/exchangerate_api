package com.dfc.exchange_api.backend.services;

import com.dfc.exchange_api.backend.exceptions.CacheNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;


@Service
public class CacheService {
    @Value("${cache.name}")
    private String CACHE_NAME;
    private static final Logger LOGGER = LoggerFactory.getLogger(CacheService.class);
    private static final String INPUT_REGEX = "[\n\r]";
    private CacheManager cacheManager;

    public CacheService(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    // CACHE MANAGEMENT ENDPOINTS

    /**
     * Gets all the entries in the exchangeRate cache
     * @return an object containing the cache entries
     * @throws CacheNotFoundException - In case the cache has not been initialized
     */
    public Object getAllCacheEntries() throws CacheNotFoundException {
        LOGGER.info("Searching for all cache entries");
        CaffeineCache caffeineCache = (CaffeineCache) cacheManager.getCache(CACHE_NAME);

        if(caffeineCache != null){
            LOGGER.info("Cache entries found");
            Map<Object, Object> cacheEntries = new HashMap<>();

            com.github.benmanes.caffeine.cache.Cache<Object, Object> nativeCache = caffeineCache.getNativeCache();
            nativeCache.asMap().forEach(cacheEntries::put);

            return cacheEntries;
        }

        LOGGER.info("No cache was found");
        throw new CacheNotFoundException("No cache was found");
    }

    /**
     * Gets all of the keys registered in the exchangeRate cache
     * @return an object containing the cache keys
     * @throws CacheNotFoundException - In case the cache has not been initialized
     */
    public Object getAllCacheKeys() throws CacheNotFoundException {
        LOGGER.info("Searching for all cache entries");
        CaffeineCache caffeineCache = (CaffeineCache) cacheManager.getCache(CACHE_NAME);

        if(caffeineCache != null){
            LOGGER.info("Cache entries found");

            com.github.benmanes.caffeine.cache.Cache<Object, Object> nativeCache = caffeineCache.getNativeCache();

            return nativeCache.asMap().keySet();
        }

        LOGGER.info("No cache was found");
        throw new CacheNotFoundException("No cache was found");
    }

    /**
     * Gets the value associated with a given key in the cache.
     * @param key - The key of the entry to be retrieved
     * @return either the correpsonding value, or null, if the key is not stored in the cache
     */
    public Object getSingleValue(@PathVariable(name = "key") String key) throws CacheNotFoundException {
        Cache cache = cacheManager.getCache(CACHE_NAME);

        if (cache != null) {
            Cache.ValueWrapper value = cache.get(key);

            if (value != null) {
                LOGGER.info("Cache entry found for key: {}", key.replaceAll(INPUT_REGEX, "_"));
                return value.get();
            }
        }else{
            throw new CacheNotFoundException("No cache was found");
        }

        LOGGER.info("No cache entry found for key: {}", key.replaceAll(INPUT_REGEX, "_"));
        return null;
    }

    /**
     * Clears the cache
     * @throws CacheNotFoundException - In case the cache has not been initialized
     */
    public void deleteAllCacheEntries() throws CacheNotFoundException {
        LOGGER.info("Searching for cache");
        Cache cache = cacheManager.getCache(CACHE_NAME);

        if(cache != null){
            LOGGER.info("Cleared the cache successfully");
            cache.clear();
        }else{
            LOGGER.info("No cache was found");
            throw new CacheNotFoundException("No cache was found");
        }
    }


    // CACHE STATISTICS ENDPOINTS

    /**
     * Returns cache statistics, such as number of hits, misses and number of evictions
     * @return A Map containing said statistics
     * @throws CacheNotFoundException - In case the cache has not been initialized
     */
    public Map<String, Object> getAllStatistics() throws CacheNotFoundException {
        LOGGER.info("Searching for cache");
        CaffeineCache caffeineCache = (CaffeineCache) cacheManager.getCache(CACHE_NAME);

        if (caffeineCache != null) {
            Map<String, Object> cacheStats = new HashMap<>();

            com.github.benmanes.caffeine.cache.Cache<Object, Object> nativeCache = caffeineCache.getNativeCache();

            com.github.benmanes.caffeine.cache.stats.CacheStats stats = nativeCache.stats();

            cacheStats.put("hits", stats.hitCount());
            cacheStats.put("misses", stats.missCount());
            cacheStats.put("evictions", stats.evictionCount());

            return cacheStats;

        }

        LOGGER.info("No cache was found");
        throw new CacheNotFoundException("No cache was found");
    }
}
