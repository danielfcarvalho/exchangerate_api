package com.dfc.exchange_api.backend.unitTests;

import com.dfc.exchange_api.backend.exceptions.CacheNotFoundException;
import com.dfc.exchange_api.backend.services.CacheService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CacheService_unitTest {
    @Mock
    private CacheManager cacheManager;
    @Mock
    private Cache exchangeRateCache;
    @Mock
    private CaffeineCache caffeineCache;

    @InjectMocks
    private CacheService cacheService;

    @Test
    void whenCacheEmpty_getAllEntries_returnEmpty(){
        // Set up Expectations
        when(cacheManager.getCache("exchangeRate")).thenReturn(caffeineCache);

        com.github.benmanes.caffeine.cache.Cache nativeCache = mock(com.github.benmanes.caffeine.cache.Cache.class);

        when(caffeineCache.getNativeCache()).thenReturn(nativeCache);
        when(nativeCache.asMap()).thenReturn(new ConcurrentHashMap() {});

        // Verify the result is as expected
        assertThat(cacheService.getAllCacheEntries()).isEqualTo(new ConcurrentHashMap() {});
    }

    @Test
    void whenCacheFull_getAllEntries_returnCorrect(){
        // Set up Expectations
        ConcurrentMap<String, Double> concurrentMap = new ConcurrentHashMap<>();
        concurrentMap.put("EUR_AMD", 422.228721);
        concurrentMap.put("EUR_ANG", 1.965639);
        concurrentMap.put("EUR_USD", 1.088186);

        when(cacheManager.getCache("exchangeRate")).thenReturn(caffeineCache);

        com.github.benmanes.caffeine.cache.Cache nativeCache = mock(com.github.benmanes.caffeine.cache.Cache.class);

        when(caffeineCache.getNativeCache()).thenReturn(nativeCache);
        when(nativeCache.asMap()).thenReturn(concurrentMap);

        // Verify the result is as expected
        assertThat(cacheService.getAllCacheEntries()).isEqualTo(concurrentMap);
    }

    @Test
    void whenNoCache_getAllEntries_throwException(){
        // Set up Expectations
        when(cacheManager.getCache("exchangeRate")).thenReturn(null);

        // Verify the result is as expected
        assertThatThrownBy(() -> cacheService.getAllCacheEntries())
                .isInstanceOf(CacheNotFoundException.class)
                .hasMessage("No cache was found");
    }

    @Test
    void whenCacheEmpty_getAllKeys_returnEmpty(){
        // Set up Expectations
        when(cacheManager.getCache("exchangeRate")).thenReturn(caffeineCache);

        com.github.benmanes.caffeine.cache.Cache nativeCache = mock(com.github.benmanes.caffeine.cache.Cache.class);

        when(caffeineCache.getNativeCache()).thenReturn(nativeCache);
        when(nativeCache.asMap()).thenReturn(new ConcurrentHashMap() {});

        // Verify the result is as expected
        assertThat(cacheService.getAllCacheKeys()).isEqualTo(new HashSet<>());
    }

    @Test
    void whenCacheFull_getAllKeys_returnCorrect(){
        // Set up Expectations
        ConcurrentMap<String, Double> concurrentMap = new ConcurrentHashMap<>();
        concurrentMap.put("EUR_AMD", 422.228721);
        concurrentMap.put("EUR_ANG", 1.965639);
        concurrentMap.put("EUR_USD", 1.088186);

        when(cacheManager.getCache("exchangeRate")).thenReturn(caffeineCache);

        com.github.benmanes.caffeine.cache.Cache nativeCache = mock(com.github.benmanes.caffeine.cache.Cache.class);

        when(caffeineCache.getNativeCache()).thenReturn(nativeCache);
        when(nativeCache.asMap()).thenReturn(concurrentMap);

        // Verify the result is as expected
        assertThat(cacheService.getAllCacheKeys()).isEqualTo(concurrentMap.keySet());
    }

    @Test
    void whenNoCache_getAllKeys_throwException(){
        // Set up Expectations
        when(cacheManager.getCache("exchangeRate")).thenReturn(null);

        // Verify the result is as expected
        assertThatThrownBy(() -> cacheService.getAllCacheKeys())
                .isInstanceOf(CacheNotFoundException.class)
                .hasMessage("No cache was found");
    }

    @Test
    void whenCacheEmpty_getSingleValue_returnEmpty(){
        // Set up Expectations
        when(cacheManager.getCache("exchangeRate")).thenReturn(exchangeRateCache);

        when(exchangeRateCache.get("EUR_AMD")).thenReturn(null);

        // Verify the result is as expected
        assertThat(cacheService.getSingleValue("EUR_AMD")).isNull();

        // Method invocation verifications
        verify(exchangeRateCache, times(1)).get(Mockito.any());
    }

    @Test
    void whenCacheFull_getSingleValue_returnCorrect(){
        // Set up Expectations
        when(cacheManager.getCache("exchangeRate")).thenReturn(exchangeRateCache);

        Cache.ValueWrapper cachedValue = mock(Cache.ValueWrapper.class);
        when(cachedValue.get()).thenReturn(422.228721);
        when(exchangeRateCache.get("EUR_AMD")).thenReturn(cachedValue);

        // Verify the result is as expected
        assertThat(cacheService.getSingleValue("EUR_AMD")).isEqualTo(422.228721);

        // Method invocation verifications
        verify(exchangeRateCache, times(1)).get(Mockito.any());
    }

    @Test
    void whenNoCache_getSingleValue_throwException(){
        // Set up Expectations
        when(cacheManager.getCache("exchangeRate")).thenReturn(null);

        // Verify the result is as expected
        assertThatThrownBy(() -> cacheService.getSingleValue("EUR_AMD"))
                .isInstanceOf(CacheNotFoundException.class)
                .hasMessage("No cache was found");
    }

    @Test
    void whenNoCache_deleteCache_throwException(){
        // Set up Expectations
        when(cacheManager.getCache("exchangeRate")).thenReturn(null);

        // Verify the result is as expected
        assertThatThrownBy(() -> cacheService.deleteAllCacheEntries())
                .isInstanceOf(CacheNotFoundException.class)
                .hasMessage("No cache was found");
    }

    @Test
    void whenCacheEmpty_getStatistics_returnEmpty(){
        // Set up Expectations
        when(cacheManager.getCache("exchangeRate")).thenReturn(caffeineCache);

        com.github.benmanes.caffeine.cache.Cache nativeCache = mock(com.github.benmanes.caffeine.cache.Cache.class);
        com.github.benmanes.caffeine.cache.stats.CacheStats cacheStats = mock(com.github.benmanes.caffeine.cache.stats.CacheStats.class);

        when(caffeineCache.getNativeCache()).thenReturn(nativeCache);
        when(nativeCache.stats()).thenReturn(cacheStats);
        when(cacheStats.hitCount()).thenReturn(0L);
        when(cacheStats.missCount()).thenReturn(0L);
        when(cacheStats.evictionCount()).thenReturn(0L);

        // Verify the result is as expected
        Map<String, Object> expectedCacheStatistics = new HashMap<>();
        expectedCacheStatistics.put("hits", 0L);
        expectedCacheStatistics.put("misses", 0L);
        expectedCacheStatistics.put("evictions", 0L);

        assertThat(cacheService.getAllStatistics()).isEqualTo(expectedCacheStatistics);
    }

    @Test
    void whenCacheFull_getStatistics_returnCorrect(){
        // Set up Expectations
        when(cacheManager.getCache("exchangeRate")).thenReturn(caffeineCache);

        com.github.benmanes.caffeine.cache.Cache nativeCache = mock(com.github.benmanes.caffeine.cache.Cache.class);
        com.github.benmanes.caffeine.cache.stats.CacheStats cacheStats = mock(com.github.benmanes.caffeine.cache.stats.CacheStats.class);

        when(caffeineCache.getNativeCache()).thenReturn(nativeCache);
        when(nativeCache.stats()).thenReturn(cacheStats);
        when(cacheStats.hitCount()).thenReturn(145L);
        when(cacheStats.missCount()).thenReturn(232L);
        when(cacheStats.evictionCount()).thenReturn(50L);

        // Verify the result is as expected
        Map<String, Object> expectedCacheStatistics = new HashMap<>();
        expectedCacheStatistics.put("hits", 145L);
        expectedCacheStatistics.put("misses", 232L);
        expectedCacheStatistics.put("evictions", 50L);
        assertThat(cacheService.getAllStatistics()).isEqualTo(expectedCacheStatistics);
    }

    @Test
    void whenNoCache_getStatistics_throwException(){
        // Set up Expectations
        when(cacheManager.getCache("exchangeRate")).thenReturn(null);

        // Verify the result is as expected
        assertThatThrownBy(() -> cacheService.getAllStatistics())
                .isInstanceOf(CacheNotFoundException.class)
                .hasMessage("No cache was found");
    }
}
