package com.dfc.exchange_api.backend.integrationTests;

import io.restassured.RestAssured;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase
class ConversionController_IT {
    private final static String BASE_URL = "http://localhost:";

    @LocalServerPort
    int randomServerPort;

    @Autowired
    CacheManager cacheManager;

    private Cache exchangeRateCache;

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
    void whenGettingConversionForVarious_withValidInput_NotInCache_thenContactExternalAPI() {
        RestAssured.given().contentType("application/json")
                .queryParams("from", "EUR", "to", "USD,GBP", "amount", "50.0")
                .when()
                .get(BASE_URL + randomServerPort + "/api/v1/convert")
                .then()
                .statusCode(200)
                .assertThat()
                .body("$", hasKey("USD")).and()
                .body("$", hasKey("GBP"));

        // Verify that values were added to the Cache
        assertThat(this.exchangeRateCache.get("EUR_USD")).isNotNull();
        assertThat(this.exchangeRateCache.get("EUR_GBP")).isNotNull();
    }

    @Test
    void whenGettingConversionForVarious_withValidInput_InCache_thenSearchInCache() {
        this.exchangeRateCache.put("EUR_USD", 100.88186);

        RestAssured.given().contentType("application/json")
                .queryParams("from", "EUR", "to", "USD,GBP", "amount", "50.0")
                .when()
                .get(BASE_URL + randomServerPort + "/api/v1/convert")
                .then()
                .statusCode(200)
                .assertThat()
                .body("$", hasKey("USD")).and()
                .body("$", hasKey("GBP")).and()
                .body("USD", equalTo(5044.093F));
    }

    @Test
    void whenGettingConversionForVarious_withInvalidFromInput_thenThrowException() {
        RestAssured.given().contentType("application/json")
                .queryParams("from", "ZZZ", "to", "USD,GBP", "amount", "50.0")
                .when()
                .get(BASE_URL + randomServerPort + "/api/v1/convert")
                .then()
                .statusCode(400)
                .body("message", equalTo("Invalid currency code ZZZ provided!"));
    }

    @Test
    void whenGettingConversionForVarious_withInvalidToInput_thenThrowException() {
        RestAssured.given().contentType("application/json")
                .queryParams("from", "EUR", "to", "USD,ZZZ", "amount", "50.0")
                .when()
                .get(BASE_URL + randomServerPort + "/api/v1/convert")
                .then()
                .statusCode(400)
                .body("message", equalTo("Invalid currency code ZZZ provided!"));
    }

    @Test
    void whenGettingConversionForVarious_withInvalidAmountInput_thenThrowException() {
        RestAssured.given().contentType("application/json")
                .queryParams("from", "ZZZ", "to", "USD,GBP", "amount", "-50.0")
                .when()
                .get(BASE_URL + randomServerPort + "/api/v1/convert")
                .then()
                .statusCode(400);
    }

    @Test
    void whenGettingConversionForSpecificCurrency_withValidInput_NotInCache_thenContactExternalAPI() {
        RestAssured.given().contentType("application/json")
                .queryParams("from", "EUR", "to", "USD", "amount", "50.0")
                .when()
                .get(BASE_URL + randomServerPort + "/api/v1/convert")
                .then()
                .statusCode(200)
                .assertThat()
                .body("$", hasKey("USD"));

        // Verify that values were added to the Cache
        assertThat(this.exchangeRateCache.get("EUR_USD")).isNotNull();
    }

    @Test
    void whenGettingConversionForSpecificCurrency_withValidInput_InCache_thenSearchInCache() {
        this.exchangeRateCache.put("EUR_USD", 100.88186);

        RestAssured.given().contentType("application/json")
                .queryParams("from", "EUR", "to", "USD", "amount", "50.0")
                .when()
                .get(BASE_URL + randomServerPort + "/api/v1/convert")
                .then()
                .statusCode(200)
                .assertThat()
                .body("$", hasKey("USD")).and()
                .body("USD", equalTo(5044.093F));
    }


    @Test
    void whenGettingConversionForSpecificCurrency_withInvalidFromInput_thenThrowException() {
        RestAssured.given().contentType("application/json")
                .queryParams("from", "ZZZ", "to", "USD", "amount", "50.0")
                .when()
                .get(BASE_URL + randomServerPort + "/api/v1/convert")
                .then()
                .statusCode(400);
    }

    @Test
    void whenGettingConversionForSpecificCurrency_withInvalidToInput_thenThrowException() {
        RestAssured.given().contentType("application/json")
                .queryParams("from", "EUR", "to", "ZZZ", "amount", "50.0")
                .when()
                .get(BASE_URL + randomServerPort + "/api/v1/convert")
                .then()
                .statusCode(400);
    }

    @Test
    void whenGettingConversionForSpecificCurrency_withInvalidAmountInput_thenThrowException() {
        RestAssured.given().contentType("application/json")
                .queryParams("from", "EUR", "to", "USD", "amount", "-50.0")
                .when()
                .get(BASE_URL + randomServerPort + "/api/v1/convert")
                .then()
                .statusCode(400);
    }
}
