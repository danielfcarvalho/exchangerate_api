package com.dfc.exchange_api.backend.unitTests;

import com.dfc.exchange_api.backend.exceptions.ExternalApiConnectionError;
import com.dfc.exchange_api.backend.exceptions.InvalidCurrencyException;
import com.dfc.exchange_api.backend.models.Currency;
import com.dfc.exchange_api.backend.repositories.CurrencyRepository;
import com.dfc.exchange_api.backend.services.ConversionService;
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
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConversionService_unitTest {
    @Mock
    ExternalApiService externalApiService;
    @Mock(lenient = true)
    CurrencyRepository currencyRepository;

    @InjectMocks
    ConversionService conversionService;

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
    void whenGettingConversionForAll_withValidInput_NotInCache_thenContactExternalAPI() {
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
                "        \"ANG\": 1.965639,\n" +
                "        \"USD\": 1.088186\n" +
                "    }\n" +
                "}";

        JsonElement rates = JsonParser.parseString(mockResponse).getAsJsonObject().get("rates");

        when(externalApiService.getLatestExchanges("EUR", Optional.of("AMD,ANG,USD"))).thenReturn(rates);
        when(currencyRepository.existsByCode("EUR")).thenReturn(true);
        when(currencyRepository.existsByCode("AMD")).thenReturn(true);
        when(currencyRepository.existsByCode("ANG")).thenReturn(true);
        when(currencyRepository.existsByCode("USD")).thenReturn(true);
        when(currencyRepository.findByCode("AMD")).thenReturn(Optional.of(dram));
        when(currencyRepository.findByCode("ANG")).thenReturn(Optional.of(guilder));
        when(currencyRepository.findByCode("USD")).thenReturn(Optional.of(dollar));

        // Verify the result is as expected
        Map<String, Double> exchangeRate = conversionService.getConversionForAll("EUR", "AMD,ANG,USD", 50.0);

        assertThat(exchangeRate).containsOnlyKeys("AMD", "ANG", "USD")
                .containsEntry("USD",54.4093)
                .containsEntry("AMD",21111.43605)
                .containsEntry("ANG",98.28195);

        // TODO: Verify that the external API was called and Verify that the cache was called twice - to query and to add the new record
        verify(externalApiService, times(1)).getLatestExchanges("EUR", Mockito.any());
    }

    @Test
    @Disabled
    void whenGettingConversionForAll_withValidInput_InConversionCache_thenSearchInConversionCache() {

    }

    @Test
    @Disabled
    void whenGettingConversionForAll_withValidInput_InExchangeCache_thenCalculateConversion() {

    }

    @Test
    void whenGettingConversionForAll_withValidInput_NotInCache_externalAPIFailure_thenThrowException() {
        // Set up Expectations
        when(currencyRepository.existsByCode("EUR")).thenReturn(true);
        when(currencyRepository.existsByCode("AMD")).thenReturn(true);
        when(currencyRepository.existsByCode("ANG")).thenReturn(true);
        when(currencyRepository.existsByCode("USD")).thenReturn(true);
        when(externalApiService.getLatestExchanges("EUR", Optional.empty())).thenThrow(new ExternalApiConnectionError("External API request failed"));

        // Verify the result is as expected
        assertThatThrownBy(() -> conversionService.getConversionForAll("EUR", "AMD,ANG,USD", 50.0))
                .isInstanceOf(ExternalApiConnectionError.class)
                .hasMessage("External API request failed");

        // TODO: Verify that the external API was called and Verify that the cache was called twice - to query and to add the new record
        verify(externalApiService, times(1)).getLatestExchanges("EUR", Optional.empty());
    }

    @Test
    void whenGettingConversionForAll_withInvalidToInput_thenThrowException() {
        // Set up Expectations
        when(currencyRepository.existsByCode("ZZZ")).thenReturn(false);
        when(currencyRepository.existsByCode("AMD")).thenReturn(true);
        when(currencyRepository.existsByCode("ANG")).thenReturn(true);
        when(currencyRepository.existsByCode("USD")).thenReturn(true);

        // Verify the result is as expected
        assertThatThrownBy(() -> conversionService.getConversionForAll("ZZZ", "AMD,ANG,USD", 50.0))
                .isInstanceOf(InvalidCurrencyException.class)
                .hasMessage("Invalid currency code provided!");

    }

    @Test
    void whenGettingConversionForAll_withInvalidFromInput_thenThrowException() {
        // Set up Expectations
        when(currencyRepository.existsByCode("ZZZ")).thenReturn(false);
        when(currencyRepository.existsByCode("AMD")).thenReturn(true);
        when(currencyRepository.existsByCode("ANG")).thenReturn(true);
        when(currencyRepository.existsByCode("USD")).thenReturn(true);

        // Verify the result is as expected
        assertThatThrownBy(() -> conversionService.getConversionForAll("AMD", "ZZZ,ANG,USD", 50.0))
                .isInstanceOf(InvalidCurrencyException.class)
                .hasMessage("Invalid currency code provided!");

    }

    @Test
    void whenGettingConversionForSpecificCurrency_withValidInput_NotInCache_thenContactExternalAPI() {
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
                "        \"USD\": \"1.088186\"\n" +
                "    }\n" +
                "}";

        JsonElement rates = JsonParser.parseString(mockResponse).getAsJsonObject().get("rates");

        when(externalApiService.getLatestExchanges("EUR", Optional.of("USD"))).thenReturn(rates);
        when(currencyRepository.existsByCode("EUR")).thenReturn(true);
        when(currencyRepository.existsByCode("USD")).thenReturn(true);

        // Verify the result is as expected
        Map<String, Double> conversionValue = conversionService.getConversionForSpecificCurrency("EUR", "USD", 50.0);

        assertThat(conversionValue).containsOnlyKeys("USD").containsEntry("USD", 54.4093);

        // TODO: Verify that the external API was called and Verify that the cache was called twice - to query and to add the new record
        verify(externalApiService, times(1)).getLatestExchanges("EUR", Optional.of("USD"));
    }

    @Test
    @Disabled
    void whenGettingConversionForSpecificCurrency_withValidInput_InConversionCache_thenSearchInConversionCache() {

    }

    @Test
    @Disabled
    void whenGettingConversionForSpecificCurrency_withValidInput_InExchangeCache_thenCalculateConversion() {

    }

    @Test
    void whenGettingConversionForSpecificCurrency_withValidInput_NotInCache_externalAPIFailure_thenThrowException() {
        // Set up Expectations
        when(currencyRepository.existsByCode("EUR")).thenReturn(true);
        when(currencyRepository.existsByCode("USD")).thenReturn(true);
        when(externalApiService.getLatestExchanges("EUR", Optional.of("USD"))).thenThrow(new ExternalApiConnectionError("External API request failed"));

        // Verify the result is as expected
        assertThatThrownBy(() -> conversionService.getConversionForSpecificCurrency("EUR", "USD", 50.0))
                .isInstanceOf(ExternalApiConnectionError.class)
                .hasMessage("External API request failed");

        // TODO: Verify that the external API was called and Verify that the cache was called twice - to query and to add the new record
        verify(externalApiService, times(1)).getLatestExchanges("EUR", Optional.of("USD"));
    }

    @Test
    void whenGettingConversionForSpecificCurrency_withInvalidFromInput_thenThrowException() {
        // Set up Expectations
        when(currencyRepository.existsByCode("EUR")).thenReturn(true);
        when(currencyRepository.existsByCode("ZZZ")).thenReturn(false);

        // Verify the result is as expected
        assertThatThrownBy(() -> conversionService.getConversionForSpecificCurrency("ZZZ","EUR", 50.0))
                .isInstanceOf(InvalidCurrencyException.class)
                .hasMessage("Invalid currency code(s) provided!");
    }

    @Test
    void whenGettingConversionForSpecificCurrency_withInvalidToInput_thenThrowException() {
        // Set up Expectations
        when(currencyRepository.existsByCode("EUR")).thenReturn(true);
        when(currencyRepository.existsByCode("ZZZ")).thenReturn(false);

        // Verify the result is as expected
        assertThatThrownBy(() -> conversionService.getConversionForSpecificCurrency("EUR","ZZZ", 50.0))
                .isInstanceOf(InvalidCurrencyException.class)
                .hasMessage("Invalid currency code(s) provided!");
    }

}
