package com.dfc.exchange_api.backend.integrationTests;

import com.dfc.exchange_api.backend.repositories.CurrencyRepository;
import io.restassured.RestAssured;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static org.hamcrest.Matchers.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase
public class CurrencyController_IT {
    private final static String BASE_URL = "http://localhost:";

    @LocalServerPort
    int randomServerPort;

    @Autowired
    CurrencyRepository currencyRepository;

    @Test
    void getSupportedCurrencies_withRepositoryEmpty() throws Exception {
        // Setting up Expectations
        currencyRepository.deleteAll();

        RestAssured.given().contentType("application/json")
                .when()
                .get(BASE_URL + randomServerPort + "/api/v1/currency")
                .then()
                .statusCode(200)
                .assertThat()
                .body("$", hasSize(0));
    }

    @Test
    void getSupportedCurrencies_withRepositoryFull() throws Exception {
        // Setting up Expectations
        RestAssured.given().contentType("application/json")
                .when()
                .get(BASE_URL + randomServerPort + "/api/v1/currency")
                .then()
                .statusCode(200)
                .assertThat()
                .body("$", Matchers.notNullValue()).and()
                .body("code", hasItems("ANG", "USD", "EUR", "SEK")).and()
                .body("name", hasItem("Euro"));
    }
}
