package com.dfc.exchange_api.backend.integrationTests;

import io.restassured.RestAssured;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase
class ExchangeController_IT {
    private final static String BASE_URL = "http://localhost:";

    @LocalServerPort
    int randomServerPort;

    @Test
    void whenGettingExchangeRateForAll_withValidInput_NotInCache_thenContactExternalAPI() {
        RestAssured.given().contentType("application/json")
                .when()
                .get(BASE_URL + randomServerPort + "/api/v1/exchange/EUR/all")
                .then()
                .statusCode(200)
                .assertThat()
                .body("$", hasKey("USD"));
    }

    @Test
    @Disabled
    void whenGettingExchangeRateForAll_withValidInput_InCache_thenSearchInCache() {

    }

    @Test
    void whenGettingExchangeRateForAll_withInvalidInput_thenThrowException() {
        RestAssured.given().contentType("application/json")
                .when()
                .get(BASE_URL + randomServerPort + "/api/v1/exchange/ZZZ/all")
                .then()
                .statusCode(400);
    }

    @Test
    void whenGettingExchangeRateForSpecificCurrency_withValidInput_NotInCache_thenContactExternalAPI() {
        RestAssured.given().contentType("application/json")
                .when()
                .get(BASE_URL + randomServerPort + "/api/v1/exchange/EUR/all?to=USD")
                .then()
                .statusCode(200)
                .assertThat()
                .body("$", hasKey("USD"));
    }

    @Test
    @Disabled
    void whenGettingExchangeRateForSpecificCurrency_withValidInput_InCache_thenSearchInCache() {

    }


    @Test
    void whenGettingExchangeRateForSpecificCurrency_withInvalidFromInput_thenThrowException() {
        RestAssured.given().contentType("application/json")
                .when()
                .get(BASE_URL + randomServerPort + "/api/v1/exchange/ZZZ/all?to=USD")
                .then()
                .statusCode(400);
    }

    @Test
    void whenGettingExchangeRateForSpecificCurrency_withInvalidToInput_thenThrowException() {
        RestAssured.given().contentType("application/json")
                .when()
                .get(BASE_URL + randomServerPort + "/api/v1/exchange/EUR?to=ZZZ")
                .then()
                .statusCode(400);
    }
}
