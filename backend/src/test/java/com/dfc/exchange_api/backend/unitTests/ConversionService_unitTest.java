package com.dfc.exchange_api.backend.unitTests;

import com.dfc.exchange_api.backend.exceptions.ExternalApiConnectionError;
import com.dfc.exchange_api.backend.exceptions.InvalidCurrencyException;
import com.dfc.exchange_api.backend.models.Currency;
import com.dfc.exchange_api.backend.repositories.CurrencyRepository;
import com.dfc.exchange_api.backend.services.ConversionService;
import com.dfc.exchange_api.backend.services.CurrencyService;
import com.dfc.exchange_api.backend.services.ExchangeService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConversionService_unitTest {
    @Mock(lenient = true)
    CurrencyRepository currencyRepository;
    @Mock
    private CurrencyService currencyService;
    @Mock
    private ExchangeService exchangeService;

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
        Map<String, Double> exchangeRatesFromExternalApi = new HashMap<>();
        exchangeRatesFromExternalApi.put("AMD", 422.228721);
        exchangeRatesFromExternalApi.put("USD", 1.088186);

        // Repository calls
        when(currencyRepository.existsByCode("EUR")).thenReturn(true);
        when(currencyRepository.existsByCode("AMD")).thenReturn(true);
        when(currencyRepository.existsByCode("USD")).thenReturn(true);

        when(currencyRepository.findByCode("AMD")).thenReturn(Optional.of(dram));
        when(currencyRepository.findByCode("USD")).thenReturn(Optional.of(dollar));

        // Exchange Service calls
        when(exchangeService.getExchangeRateFromCache("EUR", "AMD")).thenReturn(null);
        when(exchangeService.getExchangeRateFromCache("EUR", "USD")).thenReturn(null);

        when(exchangeService.getExchangeRatesFromExternalAPI("EUR", "AMD,USD,")).thenReturn(exchangeRatesFromExternalApi);

        // Verify the result is as expected
        Map<String, Double> conversions = conversionService.getConversionFromCurrency("EUR", "AMD,USD", 50.0);

        assertThat(conversions).containsOnlyKeys("AMD", "USD")
                .containsEntry("USD",54.4093)
                .containsEntry("AMD",21111.43605);

        // Method invocation verifications
        verify(currencyRepository, times(1)).existsByCode("EUR");

        verify(exchangeService, times(1)).getExchangeRateFromCache("EUR", "AMD");
        verify(exchangeService, times(1)).getExchangeRateFromCache("EUR", "USD");
        verify(exchangeService, times(1)).getExchangeRatesFromExternalAPI("EUR", "AMD,USD,");
    }

    @Test
    void whenGettingConversionForAll_withValidInput_AllInExchangeCache_thenCalculateConversion() {
        // Set up Expectations

        // Repository calls
        when(currencyRepository.existsByCode("EUR")).thenReturn(true);
        when(currencyRepository.existsByCode("AMD")).thenReturn(true);
        when(currencyRepository.existsByCode("USD")).thenReturn(true);

        // Exchange Service calls
        when(exchangeService.getExchangeRateFromCache("EUR", "AMD")).thenReturn(422.228721);
        when(exchangeService.getExchangeRateFromCache("EUR", "USD")).thenReturn(1.088186);

        // Verify the result is as expected
        Map<String, Double> conversions = conversionService.getConversionFromCurrency("EUR", "AMD,USD", 50.0);

        assertThat(conversions).containsOnlyKeys("AMD", "USD")
                .containsEntry("USD",54.4093)
                .containsEntry("AMD",21111.43605);

        // Method invocation verifications
        verify(currencyRepository, times(3)).existsByCode(Mockito.any());
        verify(exchangeService, times(1)).getExchangeRateFromCache("EUR", "AMD");
        verify(exchangeService, times(1)).getExchangeRateFromCache("EUR", "USD");
    }

    @Test
    void whenGettingConversionForAll_withValidInput_SomeInExchangeCache_thenCalculateConversion() {
        // Set up Expectations
        Map<String, Double> exchangeRatesFromExternalApi = new HashMap<>();
        exchangeRatesFromExternalApi.put("USD", 1.088186);

        // Repository calls
        when(currencyRepository.existsByCode("EUR")).thenReturn(true);
        when(currencyRepository.existsByCode("AMD")).thenReturn(true);
        when(currencyRepository.existsByCode("USD")).thenReturn(true);

        when(currencyRepository.findByCode("AMD")).thenReturn(Optional.of(dram));
        when(currencyRepository.findByCode("USD")).thenReturn(Optional.of(dollar));

        // Exchange Service calls
        when(exchangeService.getExchangeRateFromCache("EUR", "AMD")).thenReturn(422.228721);
        when(exchangeService.getExchangeRateFromCache("EUR", "USD")).thenReturn(null);

        when(exchangeService.getExchangeRatesFromExternalAPI("EUR", "USD,")).thenReturn(exchangeRatesFromExternalApi);

        // Verify the result is as expected
        Map<String, Double> conversions = conversionService.getConversionFromCurrency("EUR", "AMD,USD", 50.0);

        assertThat(conversions).containsOnlyKeys("AMD", "USD")
                .containsEntry("USD",54.4093)
                .containsEntry("AMD",21111.43605);

        // Method invocation verifications
        verify(currencyRepository, times(3)).existsByCode(Mockito.any());

        verify(exchangeService, times(1)).getExchangeRateFromCache("EUR", "AMD");
        verify(exchangeService, times(1)).getExchangeRateFromCache("EUR", "USD");
        verify(exchangeService, times(1)).getExchangeRatesFromExternalAPI("EUR", "USD,");
    }

    @Test
    void whenGettingConversionForAll_withValidInput_NotInCache_externalAPIFailure_thenThrowException() {
        // Set up Expectations
        // Repository calls
        when(currencyRepository.existsByCode("EUR")).thenReturn(true);
        when(currencyRepository.existsByCode("AMD")).thenReturn(true);
        when(currencyRepository.existsByCode("USD")).thenReturn(true);

        when(currencyRepository.findByCode("AMD")).thenReturn(Optional.of(dram));
        when(currencyRepository.findByCode("USD")).thenReturn(Optional.of(dollar));

        // Exchange Service calls
        when(exchangeService.getExchangeRateFromCache("EUR", "AMD")).thenReturn(null);
        when(exchangeService.getExchangeRateFromCache("EUR", "USD")).thenReturn(null);

        when(exchangeService.getExchangeRatesFromExternalAPI("EUR", "AMD,USD,")).thenThrow(new ExternalApiConnectionError("External API request failed"));

        // Verify the result is as expected
        assertThatThrownBy(() -> conversionService.getConversionFromCurrency("EUR", "AMD,USD", 50.0))
                .isInstanceOf(ExternalApiConnectionError.class)
                .hasMessage("External API request failed");

        // Method invocation verifications
        verify(exchangeService, times(1)).getExchangeRateFromCache("EUR", "AMD");
        verify(exchangeService, times(1)).getExchangeRateFromCache("EUR", "USD");
        verify(exchangeService, times(1)).getExchangeRatesFromExternalAPI("EUR", "AMD,USD,");
    }

    @Test
    void whenGettingConversionForAll_withInvalidToInput_thenThrowException() {
        // Set up Expectations
        when(currencyRepository.existsByCode("ZZZ")).thenReturn(false);
        when(currencyRepository.existsByCode("AMD")).thenReturn(true);
        when(currencyRepository.existsByCode("ANG")).thenReturn(true);
        when(currencyRepository.existsByCode("USD")).thenReturn(true);

        // Verify the result is as expected
        assertThatThrownBy(() -> conversionService.getConversionFromCurrency("ZZZ", "AMD,ANG,USD", 50.0))
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
        assertThatThrownBy(() -> conversionService.getConversionFromCurrency("AMD", "ZZZ,ANG,USD", 50.0))
                .isInstanceOf(InvalidCurrencyException.class)
                .hasMessage("Invalid currency code ZZZ provided!");

    }

    @Test
    void whenGettingConversionForSpecificCurrency_withValidInput_NotInCache_thenContactExternalAPI() {
        // Set up Expectations
        Map<String, Double> exchangeRatesFromExternalApi = new HashMap<>();
        exchangeRatesFromExternalApi.put("USD", 1.088186);

        // Repository calls
        when(currencyRepository.existsByCode("EUR")).thenReturn(true);
        when(currencyRepository.existsByCode("USD")).thenReturn(true);

        when(currencyRepository.findByCode("USD")).thenReturn(Optional.of(dollar));

        // Exchange Service calls
        when(exchangeService.getExchangeRateFromCache("EUR", "USD")).thenReturn(null);

        when(exchangeService.getExchangeRatesFromExternalAPI("EUR", "USD,")).thenReturn(exchangeRatesFromExternalApi);

        // Verify the result is as expected
        Map<String, Double> conversions = conversionService.getConversionFromCurrency("EUR", "USD", 50.0);

        assertThat(conversions).containsOnlyKeys("USD")
                .containsEntry("USD",54.4093);

        // Method invocation verifications
        verify(exchangeService, times(1)).getExchangeRateFromCache("EUR", "USD");
        verify(exchangeService, times(1)).getExchangeRatesFromExternalAPI("EUR", "USD,");
    }

    @Test
    void whenGettingConversionForSpecificCurrency_withValidInput_InExchangeCache_thenCalculateConversion() {
        // Set up Expectations

        // Repository calls
        when(currencyRepository.existsByCode("EUR")).thenReturn(true);
        when(currencyRepository.existsByCode("USD")).thenReturn(true);

        when(currencyRepository.findByCode("USD")).thenReturn(Optional.of(dollar));

        // Exchange Service calls
        when(exchangeService.getExchangeRateFromCache("EUR", "USD")).thenReturn(1.088186);

        // Verify the result is as expected
        Map<String, Double> conversions = conversionService.getConversionFromCurrency("EUR", "USD", 50.0);

        assertThat(conversions).containsOnlyKeys("USD")
                .containsEntry("USD",54.4093);

        // Method invocation verifications
        verify(exchangeService, times(1)).getExchangeRateFromCache("EUR", "USD");
    }

    @Test
    void whenGettingConversionForSpecificCurrency_withValidInput_NotInCache_externalAPIFailure_thenThrowException() {
        // Set up Expectations
        // Repository calls
        when(currencyRepository.existsByCode("EUR")).thenReturn(true);
        when(currencyRepository.existsByCode("USD")).thenReturn(true);

        when(currencyRepository.findByCode("USD")).thenReturn(Optional.of(dollar));

        // Exchange Service calls
        when(exchangeService.getExchangeRateFromCache("EUR", "USD")).thenReturn(null);

        when(exchangeService.getExchangeRatesFromExternalAPI("EUR", "USD,")).thenThrow(new ExternalApiConnectionError("External API request failed"));

        // Verify the result is as expected
        assertThatThrownBy(() -> conversionService.getConversionFromCurrency("EUR", "USD", 50.0))
                .isInstanceOf(ExternalApiConnectionError.class)
                .hasMessage("External API request failed");

        // Method invocation verifications
        verify(exchangeService, times(1)).getExchangeRateFromCache("EUR", "USD");
        verify(exchangeService, times(1)).getExchangeRatesFromExternalAPI("EUR", "USD,");
    }

    @Test
    void whenGettingConversionForSpecificCurrency_withInvalidFromInput_thenThrowException() {
        // Set up Expectations
        when(currencyRepository.existsByCode("EUR")).thenReturn(true);
        when(currencyRepository.existsByCode("ZZZ")).thenReturn(false);

        // Verify the result is as expected
        assertThatThrownBy(() -> conversionService.getConversionFromCurrency("ZZZ","EUR", 50.0))
                .isInstanceOf(InvalidCurrencyException.class)
                .hasMessage("Invalid currency code ZZZ provided!");
    }

    @Test
    void whenGettingConversionForSpecificCurrency_withInvalidToInput_thenThrowException() {
        // Set up Expectations
        when(currencyRepository.existsByCode("EUR")).thenReturn(true);
        when(currencyRepository.existsByCode("ZZZ")).thenReturn(false);

        // Verify the result is as expected
        assertThatThrownBy(() -> conversionService.getConversionFromCurrency("EUR","ZZZ", 50.0))
                .isInstanceOf(InvalidCurrencyException.class)
                .hasMessage("Invalid currency code ZZZ provided!");
    }

}
