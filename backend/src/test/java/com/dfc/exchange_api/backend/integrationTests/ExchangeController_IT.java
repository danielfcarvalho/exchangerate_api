package com.dfc.exchange_api.backend.integrationTests;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase
public class ExchangeController_IT {
    private final static String BASE_URI = "http://localhost:";

    @LocalServerPort
    int randomServerPort;

    @Test
    void whenGettingExchangeRateForAll_withValidInput_NotInCache_thenContactExternalAPI() {

    }

    @Test
    void whenGettingExchangeRateForAll_withValidInput_InCache_thenSearchInCache() {

    }

    @Test
    void whenGettingExchangeRateForAll_withValidInput_NotInCache_externalAPIFailure_thenThrowException() {

    }

    @Test
    void whenGettingExchangeRateForAll_withInvalidInput_thenThrowException() {

    }

    @Test
    void whenGettingExchangeRateForSpecificCurrency_withValidInput_NotInCache_thenContactExternalAPI() {

    }

    @Test
    void whenGettingExchangeRateForSpecificCurrency_withValidInput_InCache_thenSearchInCache() {

    }

    @Test
    void whenGettingExchangeRateForSpecificCurrency_withValidInput_NotInCache_externalAPIFailure_thenThrowException() {

    }

    @Test
    void whenGettingExchangeRateForSpecificCurrency_withInvalidFromInput_thenThrowException() {

    }

    @Test
    void whenGettingExchangeRateForSpecificCurrency_withInvalidToInput_thenThrowException() {

    }
}
