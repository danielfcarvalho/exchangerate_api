package com.dfc.exchange_api.backend.unitTests;

import com.dfc.exchange_api.backend.exceptions.ExternalApiConnectionError;
import com.dfc.exchange_api.backend.models.Currency;
import com.dfc.exchange_api.backend.repositories.CurrencyRepository;
import com.dfc.exchange_api.backend.services.ExchangeService;
import com.dfc.exchange_api.backend.services.ExternalApiService;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ExchangeService_unitTest {
    @Mock
    private ExternalApiService externalApiService;

    @Mock(lenient = true)
    private CurrencyRepository currencyRepository;

    @InjectMocks
    private ExchangeService exchangeService;

    Currency euro;
    Currency dollar;
    Currency dram;
    Currency guilder;

    @BeforeEach
    void setUp() {
        euro = new Currency("Euro", "EUR");
        dollar = new Currency("United States Dollar", "USD");
        dram = new Currency("Armenian Dram", "AMD");
        guilder = new Currency("Netherlands Antillean Guilder", "ANG");
    }

    @AfterEach
    void tearDown() {
        euro = null;
        dollar = null;
        dram = null;
        guilder = null;
    }

    @Test
    @Disabled
    void whenGettingExchangeRateForAll_withValidInput_NotInCache_thenContactExternalAPI() {
        // Set up Expectations
        String mockResponse = "{\n" +
                "    \"motd\": {\n" +
                "        \"msg\": \"If you or your company use this project or like what we doing, please consider backing us so we can continue maintaining and evolving this project.\",\n" +
                "        \"url\": \"https://exchangerate.host/#/donate\"\n" +
                "    },\n" +
                "    \"success\": true,\n" +
                "    \"historical\": true,\n" +
                "    \"base\": \"EUR\",\n" +
                "    \"date\": \"2023-08-17\",\n" +
                "    \"rates\": {\n" +
                "        \"AMD\": 422.228721,\n" +
                "        \"ANG\": 1.965639\n" +
                "        \"USD\": 1.088186,\n" +
                "    }\n" +
                "}";

        JsonElement rates = JsonParser.parseString(mockResponse).getAsJsonObject().get("rates");

        when(externalApiService.getLatestExchanges("EUR", Optional.empty())).thenReturn(rates);
        // TODO: Add Repository calls

        // Verify the result is as expected
        Map<String, Double> exchangeRate = exchangeService.getExchangeRateForAll("EUR");

        assertThat(exchangeRate.keySet()).containsOnly("AED", "AFN", "ALL", "AMD", "ANG", "USD");

        assertThat(exchangeRate.get(dollar)).isEqualTo(1.088186);

        // TODO: Verify that the external API was called and Verify that the cache was called twice - to query and to add the new record
        verify(externalApiService, times(1)).getLatestExchanges("EUR", Optional.empty());
    }

    @Test
    @Disabled
    void whenGettingExchangeRateForAll_withValidInput_InCache_thenSearchInCache() {

    }

    @Test
    @Disabled
    void whenGettingExchangeRateForAll_withValidInput_NotInCache_externalAPIFailure_thenThrowException() {
        // Set up Expectations
        when(externalApiService.getLatestExchanges("EUR", Optional.empty())).thenThrow(new ExternalApiConnectionError("External API request failed"));

        // Verify the result is as expected
        assertThatThrownBy(() -> exchangeService.getExchangeRateForAll("EUR"))
                .isInstanceOf(ExternalApiConnectionError.class)
                .hasMessage("External API request failed");

        // TODO: Verify that the external API was called and Verify that the cache was called twice - to query and to add the new record
        verify(externalApiService, times(1)).getLatestExchanges("EUR", Optional.empty());
    }

    @Test
    @Disabled
    void whenGettingExchangeRateForAll_withInvalidInput_thenThrowException() {
        // Set up Expectations
        // TODO: Add Repository calls

        // Verify the result is as expected
        assertThatThrownBy(() -> exchangeService.getExchangeRateForAll("ZZZ"))
                .isInstanceOf(ExternalApiConnectionError.class)
                .hasMessage("External API request failed");

    }

    @Test
    @Disabled
    void whenGettingExchangeRateForSpecificCurrency_withValidInput_NotInCache_thenContactExternalAPI() {
        // Set up Expectations
        String mockResponse = "{\n" +
                "    \"motd\": {\n" +
                "        \"msg\": \"If you or your company use this project or like what we doing, please consider backing us so we can continue maintaining and evolving this project.\",\n" +
                "        \"url\": \"https://exchangerate.host/#/donate\"\n" +
                "    },\n" +
                "    \"success\": true,\n" +
                "    \"historical\": true,\n" +
                "    \"base\": \"EUR\",\n" +
                "    \"date\": \"2023-08-17\",\n" +
                "    \"rates\": {\n" +
                "        \"USD\": 1.088186,\n" +
                "    }\n" +
                "}";

        JsonElement rates = JsonParser.parseString(mockResponse).getAsJsonObject().get("rates");

        when(externalApiService.getLatestExchanges("EUR", Optional.of("USD"))).thenReturn(rates);
        // TODO: Add Repository calls

        // Verify the result is as expected
        Map<String, Double> exchangeRate = exchangeService.getExchangeRateForSpecificCurrency("EUR", "USD");

        assertThat(exchangeRate.keySet()).containsOnly("USD");

        assertThat(exchangeRate.get(dollar)).isEqualTo(1.088186);

        // TODO: Verify that the external API was called and Verify that the cache was called twice - to query and to add the new record
        verify(externalApiService, times(1)).getLatestExchanges("EUR", Optional.of("USD"));
    }

    @Test
    @Disabled
    void whenGettingExchangeRateForSpecificCurrency_withValidInput_InCache_thenSearchInCache() {

    }

    @Test
    @Disabled
    void whenGettingExchangeRateForSpecificCurrency_withValidInput_NotInCache_externalAPIFailure_thenThrowException() {
        // Set up Expectations
        when(externalApiService.getLatestExchanges("EUR", Optional.of("USD"))).thenThrow(new ExternalApiConnectionError("External API request failed"));

        // Verify the result is as expected
        assertThatThrownBy(() -> exchangeService.getExchangeRateForSpecificCurrency("EUR", "USD"))
                .isInstanceOf(ExternalApiConnectionError.class)
                .hasMessage("External API request failed");

        // TODO: Verify that the external API was called and Verify that the cache was called twice - to query and to add the new record
        verify(externalApiService, times(1)).getLatestExchanges("EUR", Optional.of("USD"));
    }

    @Test
    @Disabled
    void whenGettingExchangeRateForSpecificCurrency_withInvalidFromInput_thenThrowException() {
        // Set up Expectations
        // TODO: Add Repository calls

        // Verify the result is as expected
        assertThatThrownBy(() -> exchangeService.getExchangeRateForSpecificCurrency("ZZZ","EUR"))
                .isInstanceOf(ExternalApiConnectionError.class)
                .hasMessage("External API request failed");
    }

    @Test
    @Disabled
    void whenGettingExchangeRateForSpecificCurrency_withInvalidToInput_thenThrowException() {
        // Set up Expectations
        // TODO: Add Repository calls

        // Verify the result is as expected
        assertThatThrownBy(() -> exchangeService.getExchangeRateForSpecificCurrency("EUR","ZZZ"))
                .isInstanceOf(ExternalApiConnectionError.class)
                .hasMessage("External API request failed");
    }
}
