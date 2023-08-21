package com.dfc.exchange_api.backend.unitTests;

import com.dfc.exchange_api.backend.exceptions.ExternalApiConnectionError;
import com.dfc.exchange_api.backend.exceptions.InvalidCurrencyException;
import com.dfc.exchange_api.backend.models.Currency;
import com.dfc.exchange_api.backend.repositories.CurrencyRepository;
import com.dfc.exchange_api.backend.services.ConversionService;
import com.dfc.exchange_api.backend.services.CurrencyService;
import com.dfc.exchange_api.backend.services.ExternalApiService;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.List;
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
    @Mock
    private CurrencyService currencyService;
    @Mock
    private CacheManager cacheManager;
    @Mock
    private Cache exchangeRateCache;

    @InjectMocks
    ConversionService conversionService;

    Currency euro;
    Currency dollar;
    Currency dram;
    List<Currency> testCurrencies;

    @BeforeEach
    void setUp() {
        euro = new Currency("Euro", "EUR");
        dollar = new Currency("United States Dollar", "USD");
        dram = new Currency("Armenian Dram", "AMD");
        testCurrencies = List.of(euro, dollar, dram);
    }

    @AfterEach
    void tearDown() {
        euro = null;
        dollar = null;
        dram = null;
        testCurrencies = null;
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
                "        \"USD\": 1.088186\n" +
                "    }\n" +
                "}";

        JsonElement rates = JsonParser.parseString(mockResponse).getAsJsonObject().get("rates");

        when(externalApiService.getLatestExchanges("EUR", Optional.of("AMD,USD,"))).thenReturn(rates);

        // Repository calls
        when(currencyRepository.existsByCode("EUR")).thenReturn(true);
        when(currencyRepository.existsByCode("AMD")).thenReturn(true);
        when(currencyRepository.existsByCode("USD")).thenReturn(true);

        when(currencyRepository.findByCode("AMD")).thenReturn(Optional.of(dram));
        when(currencyRepository.findByCode("USD")).thenReturn(Optional.of(dollar));

        // Cache calls
        when(cacheManager.getCache("exchangeRate")).thenReturn(exchangeRateCache);

        when(exchangeRateCache.get("EUR_AMD")).thenReturn(null);
        when(exchangeRateCache.get("EUR_USD")).thenReturn(null);

        // Verify the result is as expected
        Map<String, Double> exchangeRate = conversionService.getConversionForVariousCurrencies("EUR", "AMD,USD", 50.0);

        assertThat(exchangeRate).containsOnlyKeys("AMD", "USD")
                .containsEntry("USD",54.4093)
                .containsEntry("AMD",21111.43605);

        // Method invocation verifications
        verify(externalApiService, times(1)).getLatestExchanges("EUR", Optional.of("AMD,USD,"));

        verify(currencyRepository, times(1)).existsByCode("EUR");
        verify(currencyRepository, times(2)).findByCode(Mockito.any());

        verify(exchangeRateCache, times(2)).get(Mockito.any());

        verify(exchangeRateCache, times(1)).put("EUR_AMD", 422.228721);
        verify(exchangeRateCache, times(1)).put("EUR_USD", 1.088186);
    }

    @Test
    void whenGettingConversionForAll_withValidInput_AllInExchangeCache_thenCalculateConversion() {
        // Set up Expectations
        // Repository calls
        when(currencyRepository.existsByCode("EUR")).thenReturn(true);
        when(currencyRepository.existsByCode("AMD")).thenReturn(true);
        when(currencyRepository.existsByCode("USD")).thenReturn(true);

        // Cache Calls
        when(cacheManager.getCache("exchangeRate")).thenReturn(exchangeRateCache);
        Cache.ValueWrapper cachedValue = mock(Cache.ValueWrapper.class);
        when(cachedValue.get()).thenReturn(422.228721);

        when(exchangeRateCache.get("EUR_AMD")).thenReturn(cachedValue);
        when(exchangeRateCache.get("EUR_USD")).thenReturn(cachedValue);

        // Verify the result is as expected
        Map<String, Double> exchangeRate = conversionService.getConversionForVariousCurrencies("EUR", "AMD,USD", 50.0);

        assertThat(exchangeRate).containsOnlyKeys("AMD", "USD")
                .containsEntry("USD",21111.43605)
                .containsEntry("AMD",21111.43605);

        // Method invocation verifications
        verify(currencyRepository, times(3)).existsByCode(Mockito.any());

        verify(exchangeRateCache, times(2)).get(Mockito.any());
    }

    @Test
    void whenGettingConversionForAll_withValidInput_SomeInExchangeCache_thenCalculateConversion() {
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
                "        \"USD\": 1.088186\n" +
                "    }\n" +
                "}";

        JsonElement rates = JsonParser.parseString(mockResponse).getAsJsonObject().get("rates");

        when(externalApiService.getLatestExchanges("EUR", Optional.of("USD,"))).thenReturn(rates);

        // Repository calls
        when(currencyRepository.existsByCode("EUR")).thenReturn(true);
        when(currencyRepository.existsByCode("AMD")).thenReturn(true);
        when(currencyRepository.existsByCode("USD")).thenReturn(true);
        when(currencyRepository.findByCode("AMD")).thenReturn(Optional.of(dram));
        when(currencyRepository.findByCode("USD")).thenReturn(Optional.of(dollar));

        // Cache Calls
        when(cacheManager.getCache("exchangeRate")).thenReturn(exchangeRateCache);
        Cache.ValueWrapper cachedValue = mock(Cache.ValueWrapper.class);
        when(cachedValue.get()).thenReturn(422.228721);

        when(exchangeRateCache.get("EUR_AMD")).thenReturn(cachedValue);
        when(exchangeRateCache.get("EUR_USD")).thenReturn(null);

        // Verify the result is as expected
        Map<String, Double> exchangeRate = conversionService.getConversionForVariousCurrencies("EUR", "AMD,USD", 50.0);

        assertThat(exchangeRate).containsOnlyKeys("AMD", "USD")
                .containsEntry("USD",54.4093)
                .containsEntry("AMD",21111.43605);

        // Method invocation verifications
        verify(externalApiService, times(1)).getLatestExchanges("EUR", Optional.of("USD,"));

        verify(currencyRepository, times(3)).existsByCode(Mockito.any());
        verify(currencyRepository, times(1)).findByCode(Mockito.any());

        verify(exchangeRateCache, times(2)).get(Mockito.any());
        verify(exchangeRateCache, times(1)).put("EUR_USD", 1.088186);

    }

    @Test
    void whenGettingConversionForAll_withValidInput_NotInCache_externalAPIFailure_thenThrowException() {
        // Set up Expectations
        when(currencyRepository.existsByCode("EUR")).thenReturn(true);
        when(currencyRepository.existsByCode("AMD")).thenReturn(true);
        when(currencyRepository.existsByCode("USD")).thenReturn(true);
        when(externalApiService.getLatestExchanges("EUR", Optional.of("AMD,USD,"))).thenThrow(new ExternalApiConnectionError("External API request failed"));

        // Cache calls
        when(cacheManager.getCache("exchangeRate")).thenReturn(exchangeRateCache);

        when(exchangeRateCache.get("EUR_AMD")).thenReturn(null);
        when(exchangeRateCache.get("EUR_USD")).thenReturn(null);

        // Verify the result is as expected
        assertThatThrownBy(() -> conversionService.getConversionForVariousCurrencies("EUR", "AMD,USD", 50.0))
                .isInstanceOf(ExternalApiConnectionError.class)
                .hasMessage("External API request failed");

        // Method invocation verifications
        verify(externalApiService, times(1)).getLatestExchanges("EUR", Optional.of("AMD,USD,"));

        verify(exchangeRateCache, times(2)).get(Mockito.any());
    }

    @Test
    void whenGettingConversionForAll_withInvalidToInput_thenThrowException() {
        // Set up Expectations
        when(currencyRepository.existsByCode("ZZZ")).thenReturn(false);
        when(currencyRepository.existsByCode("AMD")).thenReturn(true);
        when(currencyRepository.existsByCode("ANG")).thenReturn(true);
        when(currencyRepository.existsByCode("USD")).thenReturn(true);

        // Verify the result is as expected
        assertThatThrownBy(() -> conversionService.getConversionForVariousCurrencies("ZZZ", "AMD,ANG,USD", 50.0))
                .isInstanceOf(InvalidCurrencyException.class)
                .hasMessage("Invalid currency code ZZZ provided!");

    }

    @Test
    void whenGettingConversionForAll_withInvalidFromInput_thenThrowException() {
        // Set up Expectations
        when(currencyRepository.existsByCode("ZZZ")).thenReturn(false);
        when(currencyRepository.existsByCode("AMD")).thenReturn(true);
        when(currencyRepository.existsByCode("ANG")).thenReturn(true);
        when(currencyRepository.existsByCode("USD")).thenReturn(true);

        // Verify the result is as expected
        assertThatThrownBy(() -> conversionService.getConversionForVariousCurrencies("AMD", "ZZZ,ANG,USD", 50.0))
                .isInstanceOf(InvalidCurrencyException.class)
                .hasMessage("Invalid currency code ZZZ provided!");

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

        // Cache calls
        when(cacheManager.getCache("exchangeRate")).thenReturn(exchangeRateCache);
        when(exchangeRateCache.get("EUR_USD")).thenReturn(null);

        // Repository Calls
        when(currencyRepository.existsByCode("EUR")).thenReturn(true);
        when(currencyRepository.existsByCode("USD")).thenReturn(true);

        // Verify the result is as expected
        Double conversionValue = conversionService.getConversionForSpecificCurrency("EUR", "USD", 50.0);

        assertThat(conversionValue).isEqualTo(54.4093);

        // Method invocation verifications
        verify(externalApiService, times(1)).getLatestExchanges("EUR", Optional.of("USD"));

        verify(exchangeRateCache, times(1)).get(Mockito.any());
        verify(exchangeRateCache, times(1)).put("EUR_USD", 1.088186);
    }

    @Test
    void whenGettingConversionForSpecificCurrency_withValidInput_InExchangeCache_thenCalculateConversion() {
        // Cache calls
        when(cacheManager.getCache("exchangeRate")).thenReturn(exchangeRateCache);
        Cache.ValueWrapper cachedValue = mock(Cache.ValueWrapper.class);
        when(cachedValue.get()).thenReturn(1.088186);
        when(exchangeRateCache.get("EUR_USD")).thenReturn(cachedValue);

        // Repository Calls
        when(currencyRepository.existsByCode("EUR")).thenReturn(true);
        when(currencyRepository.existsByCode("USD")).thenReturn(true);

        // Verify the result is as expected
        Double conversionValue = conversionService.getConversionForSpecificCurrency("EUR", "USD", 50.0);

        assertThat(conversionValue).isEqualTo(54.4093);

        // Method invocation verifications
        verify(exchangeRateCache, times(1)).get(Mockito.any());
    }

    @Test
    void whenGettingConversionForSpecificCurrency_withValidInput_NotInCache_externalAPIFailure_thenThrowException() {
        // Set up Expectations
        when(currencyRepository.existsByCode("EUR")).thenReturn(true);
        when(currencyRepository.existsByCode("USD")).thenReturn(true);
        when(externalApiService.getLatestExchanges("EUR", Optional.of("USD"))).thenThrow(new ExternalApiConnectionError("External API request failed"));

        // Cache calls
        when(cacheManager.getCache("exchangeRate")).thenReturn(exchangeRateCache);
        when(exchangeRateCache.get("EUR_USD")).thenReturn(null);

        // Verify the result is as expected
        assertThatThrownBy(() -> conversionService.getConversionForSpecificCurrency("EUR", "USD", 50.0))
                .isInstanceOf(ExternalApiConnectionError.class)
                .hasMessage("External API request failed");

        // Method invocation verifications
        verify(externalApiService, times(1)).getLatestExchanges("EUR", Optional.of("USD"));

        verify(exchangeRateCache, times(1)).get(Mockito.any());
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
