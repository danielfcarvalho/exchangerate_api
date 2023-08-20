package com.dfc.exchange_api.backend.integrationTests;

import io.restassured.RestAssured;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase
class ConversionController_IT {
    private final static String BASE_URL = "http://localhost:";

    @LocalServerPort
    int randomServerPort;

    @Test
    void whenGettingConversionForVarious_withValidInput_NotInCache_thenContactExternalAPI() {
        RestAssured.given().contentType("application/json")
                .when()
                .get(BASE_URL + randomServerPort + "/api/v1/convert/EUR/various?to=USD,GBP&amount=50.0")
                .then()
                .statusCode(200)
                .assertThat()
                .body("$", hasKey("USD")).and()
                .body("$", hasKey("GBP"));
    }

    @Test
    @Disabled
    void whenGettingConversionForVarious_withValidInput_InCache_thenSearchInCache() {

    }

    @Test
    void whenGettingConversionForVarious_withInvalidFromInput_thenThrowException() {
        RestAssured.given().contentType("application/json")
                .when()
                .get(BASE_URL + randomServerPort + "/api/v1/convert/ZZZ/various?to=USD,GBP&amount=50.0")
                .then()
                .statusCode(400)
                .body("message", equalTo("Invalid currency code ZZZ provided!"));
    }

    @Test
    void whenGettingConversionForVarious_withInvalidToInput_thenThrowException() {
        RestAssured.given().contentType("application/json")
                .when()
                .get(BASE_URL + randomServerPort + "/api/v1/convert/EUR/various?to=USD,ZZZ&amount=50.0")
                .then()
                .statusCode(400)
                .body("message", equalTo("Invalid currency code ZZZ provided!"));
    }

    @Test
    void whenGettingConversionForVarious_withInvalidAmountInput_thenThrowException() {
        RestAssured.given().contentType("application/json")
                .when()
                .get(BASE_URL + randomServerPort + "/api/v1/convert/EUR/various?to=USD,GBP&amount=-50.0")
                .then()
                .statusCode(400);
    }

    @Test
    void whenGettingConversionForSpecificCurrency_withValidInput_NotInCache_thenContactExternalAPI() {
        RestAssured.given().contentType("application/json")
                .when()
                .get(BASE_URL + randomServerPort + "/api/v1/convert/EUR?to=USD&amount=60.0")
                .then()
                .statusCode(200)
                .assertThat()
                .body("$", hasKey("USD"));
    }

    @Test
    @Disabled
    void whenGettingConversionForSpecificCurrency_withValidInput_InCache_thenSearchInCache() {

    }


    @Test
    void whenGettingConversionForSpecificCurrency_withInvalidFromInput_thenThrowException() {
        RestAssured.given().contentType("application/json")
                .when()
                .get(BASE_URL + randomServerPort + "/api/v1/convert/ZZZ?to=USD")
                .then()
                .statusCode(400);
    }

    @Test
    void whenGettingConversionForSpecificCurrency_withInvalidToInput_thenThrowException() {
        RestAssured.given().contentType("application/json")
                .when()
                .get(BASE_URL + randomServerPort + "/api/v1/convert/EUR?to=ZZZ")
                .then()
                .statusCode(400);
    }

    @Test
    void whenGettingConversionForSpecificCurrency_withInvalidAmountInput_thenThrowException() {
        RestAssured.given().contentType("application/json")
                .when()
                .get(BASE_URL + randomServerPort + "/api/v1/convert/EUR?to=USD&amount=-50.0")
                .then()
                .statusCode(400);
    }
}
