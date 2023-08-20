package com.dfc.exchange_api.backend.boundaryTests;

import com.dfc.exchange_api.backend.controllers.ConversionController;
import com.dfc.exchange_api.backend.exceptions.ExternalApiConnectionError;
import com.dfc.exchange_api.backend.exceptions.InvalidCurrencyException;
import com.dfc.exchange_api.backend.services.ConversionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ConversionController.class)
class Test_ConversionController_withMockService_BT_Tests {
    @Autowired
    MockMvc mockMvc;

    @MockBean
    public ConversionService conversionService;

    @Test
    void whenGettingConversionForMany_withValidInput_thenReturnOK() throws Exception {
        Map<String, Double> returnedExchanges = new HashMap<>();
        returnedExchanges.put("USD", 54.4212);
        returnedExchanges.put("GIP", 42.759);
        returnedExchanges.put("ANG", 98.0237);

        when(conversionService.getConversionForVariousCurrencies("EUR", "USD,GIP,ANG", 50.0)).thenReturn(returnedExchanges);

        mockMvc.perform(
                        get("/api/v1/convert/EUR/various")
                                .param("to", "USD,GIP,ANG")
                                .param("amount", "50.0").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.USD", is(54.4212)))
                .andExpect(jsonPath("$.GIP", is(42.759)))
                .andExpect(jsonPath("$.ANG", is(98.0237)));
    }

    @Test
    void whenGettingConversionForMany_withValidInput_externalAPIFailure_thenThrowException() throws Exception {
        when(conversionService.getConversionForVariousCurrencies("EUR", "USD,GIP,ANG", 50.0)).thenThrow(ExternalApiConnectionError.class);

        mockMvc.perform(
                        get("/api/v1/convert/EUR/various")
                                .param("to", "USD,GIP,ANG")
                                .param("amount", "50.0")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadGateway());
    }

    @Test
    void whenGettingConversionForMany_withInvalidFromInput_thenThrowException() throws Exception {
        when(conversionService.getConversionForVariousCurrencies("ZZZ", "USD,GIP,ANG", 50.0)).thenThrow(InvalidCurrencyException.class);

        mockMvc.perform(
                        get("/api/v1/convert/ZZZ/various")
                                .param("to", "USD,GIP,ANG")
                                .param("amount", "50.0")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenGettingConversionForMany_withInvalidToInput_thenThrowException() throws Exception {
        when(conversionService.getConversionForVariousCurrencies("USD", "ZZZ,GIP,ANG", 50.0)).thenThrow(InvalidCurrencyException.class);

        mockMvc.perform(
                        get("/api/v1/convert/USD/various")
                                .param("to", "ZZZ,GIP,ANG")
                                .param("amount", "50.0")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenGettingConversionForMany_withInvalidAmountInput_thenThrowException() throws Exception {
        mockMvc.perform(
                        get("/api/v1/convert/EUR/various")
                                .param("to", "USD,GIP,ANG")
                                .param("amount", "-50.0")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenGettingConversionForSpecificCurrency_withValidInput_thenReturnOK() throws Exception {
        Map<String, Double> returnedExchanges = new HashMap<>();
        returnedExchanges.put("USD", 54.4212);

        when(conversionService.getConversionForSpecificCurrency("EUR", "USD", 50.0)).thenReturn(returnedExchanges);

        mockMvc.perform(
                        get("/api/v1/convert/EUR")
                                .param("to", "USD")
                                .param("amount", "50.0").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.USD", is(54.4212)));
    }

    @Test
    void whenGettingConversionForSpecificCurrency_withValidInput_externalAPIFailure_thenThrowException() throws Exception {
        when(conversionService.getConversionForSpecificCurrency("EUR", "USD", 50.0)).thenThrow(ExternalApiConnectionError.class);

        mockMvc.perform(
                        get("/api/v1/convert/EUR")
                                .param("to", "USD")
                                .param("amount", "50.0").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadGateway());
    }

    @Test
    void whenGettingConversionForSpecificCurrency_withInvalidFromInput_thenThrowException() throws Exception {
        when(conversionService.getConversionForSpecificCurrency("ZZZ", "USD", 50.0)).thenThrow(InvalidCurrencyException.class);

        mockMvc.perform(
                        get("/api/v1/convert/ZZZ")
                                .param("to", "USD")
                                .param("amount", "50.0").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenGettingConversionForSpecificCurrency_withInvalidToInput_thenThrowException() throws Exception {
        when(conversionService.getConversionForSpecificCurrency("USD", "ZZZ", 50.0)).thenThrow(InvalidCurrencyException.class);

        mockMvc.perform(
                        get("/api/v1/convert/USD")
                                .param("to", "ZZZ")
                                .param("amount", "50.0").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenGettingConversionForSpecificCurrency_withInvalidAmountInput_thenThrowException() throws Exception {
        mockMvc.perform(
                        get("/api/v1/convert/EUR")
                                .param("to", "USD")
                                .param("amount", String.valueOf(-50.0))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
