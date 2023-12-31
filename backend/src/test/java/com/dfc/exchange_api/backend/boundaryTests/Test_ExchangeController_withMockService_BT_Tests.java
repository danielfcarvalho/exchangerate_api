package com.dfc.exchange_api.backend.boundaryTests;

import com.dfc.exchange_api.backend.controllers.ExchangeController;
import com.dfc.exchange_api.backend.exceptions.ExternalApiConnectionError;
import com.dfc.exchange_api.backend.exceptions.InvalidCurrencyException;
import com.dfc.exchange_api.backend.services.ExchangeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ExchangeController.class)
class Test_ExchangeController_withMockService_BT_Tests {
    @Autowired
    MockMvc mockMvc;

    @MockBean
    private ExchangeService exchangeService;

    @Test
    void whenGettingExchangeRateForAll_withValidInput_thenReturnOK() throws Exception {
        Map<String, Double> returnedExchanges = new HashMap<>();
        returnedExchanges.put("USD", 1.088424);
        returnedExchanges.put("GIP", 0.85518);
        returnedExchanges.put("ANG", 1.960474);

        when(exchangeService.getExchangeRateForAll("EUR")).thenReturn(returnedExchanges);

        mockMvc.perform(
                        get("/api/v1/exchange")
                                .param("from", "EUR").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.USD", is(1.088424)))
                .andExpect(jsonPath("$.GIP", is(0.85518)))
                .andExpect(jsonPath("$.ANG", is(1.960474)));
    }

    @Test
    void whenGettingExchangeRateForAll_withValidInput_externalAPIFailure_thenThrowException() throws Exception {
        when(exchangeService.getExchangeRateForAll("EUR")).thenThrow(ExternalApiConnectionError.class);

        mockMvc.perform(
                        get("/api/v1/exchange")
                                .param("from", "EUR").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    void whenGettingExchangeRateForAll_withInvalidInput_thenThrowException() throws Exception {
        when(exchangeService.getExchangeRateForAll("ZZZ")).thenThrow(InvalidCurrencyException.class);

        mockMvc.perform(
                        get("/api/v1/exchange")
                                .param("from", "ZZZ").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenGettingExchangeRateForSpecificCurrency_withValidInput_thenReturnOK() throws Exception {
        when(exchangeService.getExchangeRateForSpecificCurrency("EUR", "USD")).thenReturn(1.088424);

        mockMvc.perform(
                        get("/api/v1/exchange")
                                .param("from", "EUR")
                                .param("to", "USD").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.USD", is(1.088424)));
    }

    @Test
    void whenGettingExchangeRateForSpecificCurrency_withValidInput_externalAPIFailure_thenThrowException() throws Exception {
        when(exchangeService.getExchangeRateForSpecificCurrency("EUR", "USD")).thenThrow(ExternalApiConnectionError.class);

        mockMvc.perform(
                        get("/api/v1/exchange")
                                .param("from", "EUR")
                                .param("to", "USD").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    void whenGettingExchangeRateForSpecificCurrency_withInvalidFromInput_thenThrowException() throws Exception {
        when(exchangeService.getExchangeRateForSpecificCurrency("ZZZ", "USD")).thenThrow(InvalidCurrencyException.class);

        mockMvc.perform(
                        get("/api/v1/exchange")
                                .param("from", "ZZZ")
                                .param("to", "USD").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenGettingExchangeRateForSpecificCurrency_withInvalidToInput_thenThrowException() throws Exception {
        when(exchangeService.getExchangeRateForSpecificCurrency("EUR", "ZZZ")).thenThrow(InvalidCurrencyException.class);

        mockMvc.perform(
                        get("/api/v1/exchange")
                                .param("from", "EUR")
                                .param("to", "ZZZ").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
