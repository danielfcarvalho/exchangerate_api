package com.dfc.exchange_api.backend.boundaryTests;

import com.dfc.exchange_api.backend.controllers.ExchangeController;
import com.dfc.exchange_api.backend.services.ExchangeService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = ExchangeController.class)
public class ExchangeController_withMockService_BT {
    @Autowired
    MockMvc mockMvc;

    @MockBean
    private ExchangeService exchangeService;

    @BeforeEach
    void setUp(){

    }

    @AfterEach
    void tearDown(){

    }

    @Test
    void whenGettingExchangeRateForAll_withValidInput_thenReturnOK() {

    }

    @Test
    void whenGettingExchangeRateForAll_withValidInput_externalAPIFailure_thenThrowException() {

    }

    @Test
    void whenGettingExchangeRateForAll_withInvalidInput_thenThrowException() {

    }

    @Test
    void whenGettingExchangeRateForSpecificCurrency_withValidInput_thenReturnOK() {

    }

    @Test
    void whenGettingExchangeRateForSpecificCurrency_withValidInput_externalAPIFailure_thenThrowException() {

    }

    @Test
    void whenGettingExchangeRateForSpecificCurrency_withInvalidFromInput_thenThrowException() {

    }

    @Test
    void whenGettingExchangeRateForSpecificCurrency_withInvalidToInput_thenThrowException() {

    }
}
