package com.dfc.exchange_api.backend.integrationTests;

import io.restassured.RestAssured;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import static org.hamcrest.Matchers.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase
class ExchangeController_IT {
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
    void whenGettingExchangeRateForAll_withValidInput_NotInCache_thenContactExternalAPI() {
        RestAssured.given().contentType("application/json")
                .queryParams("from", "EUR")
                .when()
                .get(BASE_URL + randomServerPort + "/api/v1/exchange")
                .then()
                .statusCode(200)
                .assertThat()
                .body("$", hasKey("USD"));

        // Verify that values were added to the Cache
        assertThat(this.exchangeRateCache.get("EUR_USD")).isNotNull();
        assertThat(this.exchangeRateCache.get("EUR_SEK")).isNotNull();
    }

    @Test
    void whenGettingExchangeRateForAll_withValidInput_InCache_thenSearchInCache() {
        this.exchangeRateCache.put("EUR_USD", 101010.2);

        RestAssured.given().contentType("application/json")
                .queryParams("from", "EUR")
                .when()
                .get(BASE_URL + randomServerPort + "/api/v1/exchange")
                .then()
                .statusCode(200)
                .assertThat()
                .body("$", hasKey("USD")).and()
                .body("$", hasKey("SEK")).and()
                .body("USD", equalTo(101010.2f));
    }

    @Test
    void whenGettingExchangeRateForAll_withInvalidInput_thenThrowException() {
        RestAssured.given().contentType("application/json")
                .queryParams("from", "ZZZ")
                .when()
                .get(BASE_URL + randomServerPort + "/api/v1/exchange")
                .then()
                .statusCode(400);
    }

    @Test
    void whenGettingExchangeRateForSpecificCurrency_withValidInput_NotInCache_thenContactExternalAPI() {
        RestAssured.given().contentType("application/json")
                .queryParams("from", "EUR", "to", "USD")
                .when()
                .get(BASE_URL + randomServerPort + "/api/v1/exchange")
                .then()
                .statusCode(200)
                .assertThat()
                .body("$", hasKey("USD"));

        // Verify that values were added to the Cache
        assertThat(this.exchangeRateCache.get("EUR_USD")).isNotNull();
    }

    @Test
    void whenGettingExchangeRateForSpecificCurrency_withValidInput_InCache_thenSearchInCache() {
        this.exchangeRateCache.put("EUR_USD", 101010.2);

        RestAssured.given().contentType("application/json")
                .queryParams("from", "EUR", "to", "USD")
                .when()
                .get(BASE_URL + randomServerPort + "/api/v1/exchange")
                .then()
                .statusCode(200)
                .assertThat()
                .body("$", hasKey("USD")).and()
                .body("USD", equalTo(101010.2f));
    }


    @Test
    void whenGettingExchangeRateForSpecificCurrency_withInvalidFromInput_thenThrowException() {
        RestAssured.given().contentType("application/json")
                .queryParams("from", "ZZZ", "to", "USD")
                .when()
                .get(BASE_URL + randomServerPort + "/api/v1/exchange")
                .then()
                .statusCode(400);
    }

    @Test
    void whenGettingExchangeRateForSpecificCurrency_withInvalidToInput_thenThrowException() {
        RestAssured.given().contentType("application/json")
                .queryParams("from", "EUR", "to", "ZZZ")
                .when()
                .get(BASE_URL + randomServerPort + "/api/v1/exchange")
                .then()
                .statusCode(400);
    }
}
