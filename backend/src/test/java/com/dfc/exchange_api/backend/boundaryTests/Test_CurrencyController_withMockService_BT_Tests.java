package com.dfc.exchange_api.backend.boundaryTests;

import com.dfc.exchange_api.backend.controllers.CurrencyController;
import com.dfc.exchange_api.backend.models.Currency;
import com.dfc.exchange_api.backend.services.CurrencyService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CurrencyController.class)
class Test_CurrencyController_withMockService_BT_Tests {
    @Autowired
    MockMvc mockMvc;

    @MockBean
    public CurrencyService currencyService;

    @Test
    void getSupportedCurrencies_withRepositoryEmpty() throws Exception {
        // Setting up Expectations
        when(currencyService.getSupportedCurrencies()).thenReturn(Collections.emptyList());

        mockMvc.perform(
                        get("/api/v1/currency").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", Matchers.empty()));
    }

    @Test
    void getSupportedCurrencies_withRepositoryFull() throws Exception {
        // Setting up Expectations
        Currency dirham = new Currency("United Arab Emirates Dirham", "AED");
        Currency afghani = new Currency("Afghan Afghani", "AFN");
        Currency lek = new Currency("Albanian Lek", "ALL");
        Currency euro = new Currency("Euro", "EUR");

        List<Currency> currenciesOnRepo = Arrays.asList(dirham, afghani, lek, euro);
        when(currencyService.getSupportedCurrencies()).thenReturn(currenciesOnRepo);

        mockMvc.perform(
                        get("/api/v1/currency").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].code", is("AED")))
                .andExpect(jsonPath("$.[1].code", is("AFN")))
                .andExpect(jsonPath("$.[2].code", is("ALL")))
                .andExpect(jsonPath("$.[3].code", is("EUR")));
    }
}
