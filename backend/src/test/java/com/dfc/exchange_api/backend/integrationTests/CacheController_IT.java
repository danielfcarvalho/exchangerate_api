package com.dfc.exchange_api.backend.integrationTests;

import io.restassured.RestAssured;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CacheController_IT {
    private final static String BASE_URL = "http://localhost:";

    @Autowired
    CacheManager cacheManager;

    private Cache exchangeRateCache;

    @LocalServerPort
    int randomServerPort;

    @BeforeEach
    void setUp(){
        this.exchangeRateCache = cacheManager.getCache("exchangeRate");
    }

    @AfterEach
    void tearDown(){
        this.exchangeRateCache.clear();
        this.exchangeRateCache = null;
    }

    @Test
    void whenGetAllEntries_cacheFull_thenReturnOK(){
        // Adding entries to the cache
        exchangeRateCache.put("EUR_USD", 1.088186);
        exchangeRateCache.put("EUR_ANG", 1.965639);

        RestAssured.given().contentType("application/json")
                .when()
                .get(BASE_URL + randomServerPort + "/api/v1/cache/entries/all")
                .then()
                .statusCode(200)
                .assertThat()
                .body("$", hasKey("EUR_USD")).and()
                .body("$", hasKey("EUR_ANG")).and()
                .body("EUR_USD", equalTo(1.088186F));
    }

    @Test
    void whenGetAllEntries_cacheEmpty_thenReturnOK(){
        RestAssured.given().contentType("application/json")
                .when()
                .get(BASE_URL + randomServerPort + "/api/v1/cache/entries/all")
                .then()
                .statusCode(200)
                .assertThat()
                .body(equalTo("{}"));
    }

    @Test
    void whenGetAllKeys_cacheFull_thenReturnOK() {
        // Adding entries to the cache
        exchangeRateCache.put("EUR_USD", 1.088186);
        exchangeRateCache.put("EUR_ANG", 1.965639);

        RestAssured.given().contentType("application/json")
                .when()
                .get(BASE_URL + randomServerPort + "/api/v1/cache/entries/keys")
                .then()
                .statusCode(200)
                .assertThat()
                .body("$", hasItems("EUR_USD", "EUR_ANG"));
    }

    @Test
    void whenGetAllKeys_cacheEmpty_thenReturnOK(){
        RestAssured.given().contentType("application/json")
                .when()
                .get(BASE_URL + randomServerPort + "/api/v1/cache/entries/keys")
                .then()
                .statusCode(200)
                .assertThat()
                .body("$", hasItems());
    }

    @Test
    void whenGetSingleValue_cacheFull_thenReturnOK() throws Exception {
        // Adding entries to the cache
        exchangeRateCache.put("EUR_USD", 1.088186);
        exchangeRateCache.put("EUR_ANG", 1.965639);

        RestAssured.given().contentType("application/json")
                .when()
                .get(BASE_URL + randomServerPort + "/api/v1/cache/entries/EUR_USD")
                .then()
                .statusCode(200);
    }

    @Test
    void whenGetSingleValue_cacheEmpty_thenReturnNotFound() throws Exception {
        RestAssured.given().contentType("application/json")
                .when()
                .get(BASE_URL + randomServerPort + "/api/v1/cache/entries/EUR_USD")
                .then()
                .statusCode(404);
    }

    @Test
    void whenDeleteCache_cacheFull_thenReturnOK() throws Exception {
        // Adding entries to the cache
        exchangeRateCache.put("EUR_USD", 1.088186);
        exchangeRateCache.put("EUR_ANG", 1.965639);

        RestAssured.given().contentType("application/json")
                .when()
                .delete(BASE_URL + randomServerPort + "/api/v1/cache/entries/all")
                .then()
                .statusCode(200);

        // Verify that the cache is indeed empty
        assertThat(this.exchangeRateCache.get("EUR_USD")).isNull();
        assertThat(this.exchangeRateCache.get("EUR_ANG")).isNull();
    }

    @Test
    @Order(2)
    void whenGetAllStatistics_cacheWithStats_thenReturnOK() throws Exception {
        // Adding entries to the cache
        exchangeRateCache.put("EUR_USD", 1.088186);
        exchangeRateCache.put("EUR_ANG", 1.965639);

        exchangeRateCache.get("EUR_USD");
        exchangeRateCache.get("EUR_ANG");
        exchangeRateCache.get("EUR_GBP");

        RestAssured.given().contentType("application/json")
                .when()
                .get(BASE_URL + randomServerPort + "/api/v1/cache/statistics/all")
                .then()
                .statusCode(200)
                .assertThat()
                .body("hits", equalTo(2)).and()
                .body("misses", equalTo(1)).and()
                .body("evictions", equalTo(0)).and();
    }

    @Test
    @Order(1)
    void whenGetAllStatistics_cacheEmpty_thenReturnOK() throws Exception {
        RestAssured.given().contentType("application/json")
                .when()
                .get(BASE_URL + randomServerPort + "/api/v1/cache/statistics/all")
                .then()
                .statusCode(200)
                .assertThat()
                .body("hits", equalTo(0)).and()
                .body("misses", equalTo(0)).and()
                .body("evictions", equalTo(0)).and();
    }
}
