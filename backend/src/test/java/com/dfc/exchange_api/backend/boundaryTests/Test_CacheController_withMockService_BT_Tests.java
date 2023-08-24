package com.dfc.exchange_api.backend.boundaryTests;

import com.dfc.exchange_api.backend.controllers.CacheController;
import com.dfc.exchange_api.backend.exceptions.CacheNotFoundException;
import com.dfc.exchange_api.backend.services.CacheService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CacheController.class)
class Test_CacheController_withMockService_BT_Tests {
    @Autowired
    MockMvc mockMvc;

    @MockBean
    public CacheService cacheService;

    @Test
    void whenGetAllEntries_cacheExists_thenReturnOK() throws Exception {
        ConcurrentMap<String, Double> concurrentMap = new ConcurrentHashMap<>();
        concurrentMap.put("EUR_AMD", 422.228721);
        concurrentMap.put("EUR_ANG", 1.965639);
        concurrentMap.put("EUR_USD", 1.088186);

        when(cacheService.getAllCacheEntries()).thenReturn(concurrentMap);

        mockMvc.perform(
                        get("/api/v1/cache/entries/all").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.EUR_AMD", is(422.228721)))
                .andExpect(jsonPath("$.EUR_ANG", is(1.965639)))
                .andExpect(jsonPath("$.EUR_USD", is(1.088186)));
    }

    @Test
    void whenGetAllEntries_cacheExists_thenThrowException() throws Exception {
        when(cacheService.getAllCacheEntries()).thenThrow(CacheNotFoundException.class);

        mockMvc.perform(
                        get("/api/v1/cache/entries/all").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void whenGetAllKeys_cacheExists_thenReturnOK() throws Exception {
        Set<String> keySet = new HashSet<>();
        keySet.add("EUR_AMD");
        keySet.add("EUR_ANG");
        keySet.add("EUR_USD");

        when(cacheService.getAllCacheKeys()).thenReturn(keySet);

        mockMvc.perform(
                        get("/api/v1/cache/entries/keys").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]", is("EUR_USD")))
                .andExpect(jsonPath("$[1]", is("EUR_AMD")))
                .andExpect(jsonPath("$[2]", is("EUR_ANG")));
    }

    @Test
    void whenGetAllKeys_cacheExists_thenThrowException() throws Exception {
        when(cacheService.getAllCacheKeys()).thenThrow(CacheNotFoundException.class);

        mockMvc.perform(
                        get("/api/v1/cache/entries/keys").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void whenGetSingleValue_cacheExists_thenReturnOK() throws Exception {
        when(cacheService.getSingleValue("EUR_USD")).thenReturn(1.088186);

        mockMvc.perform(
                        get("/api/v1/cache/entries/EUR_USD").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$",is(1.088186)));
    }

    @Test
    void whenGetSingleValue_cacheExists_thenThrowException() throws Exception {
        when(cacheService.getSingleValue("EUR_USD")).thenThrow(CacheNotFoundException.class);

        mockMvc.perform(
                        get("/api/v1/cache/entries/EUR_USD").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void whenDeleteCache_cacheExists_thenReturnOK() throws Exception {
        mockMvc.perform(
                        delete("/api/v1/cache/entries/all").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$",is("Cache cleared successfully")));
    }

    @Test
    void whenGetAllStatistics_cacheExists_thenReturnOK() throws Exception {
        Map<String, Object> statsMap = new HashMap<>();
        statsMap.put("hits", 42);
        statsMap.put("misses", 1);
        statsMap.put("evictions", 1);

        when(cacheService.getAllStatistics()).thenReturn(statsMap);
        mockMvc.perform(
                        get("/api/v1/cache/statistics/all").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hits", is(42)))
                .andExpect(jsonPath("$.misses", is(1)))
                .andExpect(jsonPath("$.evictions", is(1)));
    }

    @Test
    void whenGetAllStatistics_cacheExists_thenThrowException() throws Exception {
        when(cacheService.getAllStatistics()).thenThrow(CacheNotFoundException.class);

        mockMvc.perform(
                        get("/api/v1/cache/statistics/all").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
