package com.dfc.exchange_api.backend.unitTests;

import com.dfc.exchange_api.backend.exceptions.ExternalApiConnectionError;
import com.dfc.exchange_api.backend.exceptions.InvalidCurrencyException;
import com.dfc.exchange_api.backend.models.Currency;
import com.dfc.exchange_api.backend.models.ExchangeRateDTO;
import com.dfc.exchange_api.backend.repositories.CurrencyRepository;
import com.dfc.exchange_api.backend.services.CurrencyService;
import com.dfc.exchange_api.backend.services.ExchangeService;
import com.dfc.exchange_api.backend.services.ExternalApiService;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExchangeService_unitTest {
    @Mock
    private ExternalApiService externalApiService;
    @Mock(lenient = true)
    private CurrencyRepository currencyRepository;
    @Mock
    private CurrencyService currencyService;
    @Mock
    private CacheManager cacheManager;
    @Mock
    private Cache exchangeRateCache;

    @InjectMocks
    private ExchangeService exchangeService;

    Currency euro;
    Currency dollar;
    Currency dram;
    Currency guilder;
    List<Currency> testCurrencies;

    @BeforeEach
    void setUp() {
        euro = new Currency("Euro", "EUR");
        dollar = new Currency("United States Dollar", "USD");
        dram = new Currency("Armenian Dram", "AMD");
        guilder = new Currency("Netherlands Antillean Guilder", "ANG");
        testCurrencies = List.of(euro, dollar, dram, guilder);
    }

    @AfterEach
    void tearDown() {
        euro = null;
        dollar = null;
        dram = null;
        guilder = null;
        testCurrencies = null;
    }

    @Test
    void whenGettingExchangeRateForAll_withValidInput_NotInCache_thenContactExternalAPI() {
        // Set up Expectations
        // External API Call
        HashMap<String, Double> returnedRates = new HashMap<>();

        returnedRates.put("USD", 1.088186);
        returnedRates.put("AMD", 422.228721);
        returnedRates.put("ANG", 1.965639);

        ExchangeRateDTO ratesDTO = new ExchangeRateDTO();
        ratesDTO.setRates(returnedRates);

        when(externalApiService.getLatestExchanges("EUR", Optional.of("EUR,USD,AMD,ANG,"))).thenReturn(ratesDTO);

        // Repository calls
        when(currencyRepository.existsByCode("EUR")).thenReturn(true);
        when(currencyRepository.findByCode("AMD")).thenReturn(Optional.of(dram));
        when(currencyRepository.findByCode("ANG")).thenReturn(Optional.of(guilder));
        when(currencyRepository.findByCode("USD")).thenReturn(Optional.of(dollar));
        when(currencyRepository.findAll()).thenReturn(testCurrencies);

        // Cache calls
        when(cacheManager.getCache(Mockito.any())).thenReturn(exchangeRateCache);
        when(exchangeRateCache.get("EUR_AMD")).thenReturn(null);
        when(exchangeRateCache.get("EUR_ANG")).thenReturn(null);
        when(exchangeRateCache.get("EUR_USD")).thenReturn(null);
        when(exchangeRateCache.get("EUR_EUR")).thenReturn(null);

        // Verify the result is as expected
        Map<String, Double> exchangeRate = exchangeService.getExchangeRateForAll("EUR");

        assertThat(exchangeRate).containsOnlyKeys("AMD", "ANG", "USD").containsEntry("USD",1.088186);

        // Method invocation verifications
        verify(externalApiService, times(1)).getLatestExchanges("EUR", Optional.of("EUR,USD,AMD,ANG,"));

        verify(currencyRepository, times(1)).existsByCode("EUR");
        verify(currencyRepository, times(3)).findByCode(Mockito.any());
        verify(currencyRepository, times(1)).findAll();

        verify(exchangeRateCache, times(4)).get(Mockito.any());

        verify(exchangeRateCache, times(1)).put("EUR_AMD", 422.228721);
        verify(exchangeRateCache, times(1)).put("EUR_ANG", 1.965639);
        verify(exchangeRateCache, times(1)).put("EUR_USD", 1.088186);
    }

    @Test
    void whenGettingExchangeRateForAll_withValidInput_AllInCache_thenSearchInCache() {
        // Set up Expectations
        // Repository calls
        when(currencyRepository.existsByCode("EUR")).thenReturn(true);
        when(currencyRepository.findAll()).thenReturn(testCurrencies);

        // Cache Calls
        when(cacheManager.getCache(Mockito.any())).thenReturn(exchangeRateCache);
        Cache.ValueWrapper cachedValue = mock(Cache.ValueWrapper.class);
        when(cachedValue.get()).thenReturn(422.228721);

        when(exchangeRateCache.get("EUR_AMD")).thenReturn(cachedValue);
        when(exchangeRateCache.get("EUR_ANG")).thenReturn(cachedValue);
        when(exchangeRateCache.get("EUR_USD")).thenReturn(cachedValue);
        when(exchangeRateCache.get("EUR_EUR")).thenReturn(cachedValue);


        // Verify the result is as expected
        Map<String, Double> exchangeRate = exchangeService.getExchangeRateForAll("EUR");

        assertThat(exchangeRate).containsOnlyKeys("AMD", "ANG", "USD", "EUR").containsEntry("USD",422.228721);

        // Method invocation verifications
        verify(currencyRepository, times(1)).existsByCode("EUR");
        verify(currencyRepository, times(1)).findAll();

        verify(exchangeRateCache, times(4)).get(Mockito.any());
    }

    @Test
    void whenGettingExchangeRateForAll_withValidInput_SomeInCache_thenSearchInCache() {
        // Set up Expectations
        HashMap<String, Double> returnedRates = new HashMap<>();

        returnedRates.put("AMD", 422.228721);
        returnedRates.put("ANG", 1.965639);

        ExchangeRateDTO ratesDTO = new ExchangeRateDTO();
        ratesDTO.setRates(returnedRates);

        when(externalApiService.getLatestExchanges("EUR", Optional.of("AMD,ANG,"))).thenReturn(ratesDTO);

        // Repository calls
        when(currencyRepository.existsByCode("EUR")).thenReturn(true);
        when(currencyRepository.findByCode("AMD")).thenReturn(Optional.of(dram));
        when(currencyRepository.findByCode("ANG")).thenReturn(Optional.of(guilder));
        when(currencyRepository.findAll()).thenReturn(testCurrencies);

        // Cache calls
        when(cacheManager.getCache(Mockito.any())).thenReturn(exchangeRateCache);
        Cache.ValueWrapper cachedValue = mock(Cache.ValueWrapper.class);
        when(cachedValue.get()).thenReturn(422.228721);

        when(exchangeRateCache.get("EUR_AMD")).thenReturn(null);
        when(exchangeRateCache.get("EUR_ANG")).thenReturn(null);
        when(exchangeRateCache.get("EUR_USD")).thenReturn(cachedValue);
        when(exchangeRateCache.get("EUR_EUR")).thenReturn(cachedValue);


        // Verify the result is as expected
        Map<String, Double> exchangeRate = exchangeService.getExchangeRateForAll("EUR");

        assertThat(exchangeRate).containsOnlyKeys("AMD", "ANG", "USD", "EUR")
                .containsEntry("USD",422.228721)
                .containsEntry("ANG", 1.965639);

        // Method invocation verifications
        verify(externalApiService, times(1)).getLatestExchanges("EUR", Optional.of("AMD,ANG,"));

        verify(currencyRepository, times(1)).existsByCode("EUR");
        verify(currencyRepository, times(2)).findByCode(Mockito.any());
        verify(currencyRepository, times(1)).findAll();

        verify(exchangeRateCache, times(4)).get(Mockito.any());

        verify(exchangeRateCache, times(1)).put("EUR_AMD", 422.228721);
        verify(exchangeRateCache, times(1)).put("EUR_ANG", 1.965639);
    }

    @Test
    void whenGettingExchangeRateForAll_withValidInput_NotInCache_externalAPIFailure_thenThrowException() {
        // Set up Expectations
        when(externalApiService.getLatestExchanges("EUR", Optional.of("EUR,USD,AMD,ANG,"))).thenThrow(new ExternalApiConnectionError("External API request failed"));

        // Repository calls
        when(currencyRepository.existsByCode("EUR")).thenReturn(true);
        when(currencyRepository.findByCode("AMD")).thenReturn(Optional.of(dram));
        when(currencyRepository.findByCode("ANG")).thenReturn(Optional.of(guilder));
        when(currencyRepository.findByCode("USD")).thenReturn(Optional.of(dollar));
        when(currencyRepository.findAll()).thenReturn(testCurrencies);

        // Cache calls
        when(cacheManager.getCache(Mockito.any())).thenReturn(exchangeRateCache);
        when(exchangeRateCache.get("EUR_AMD")).thenReturn(null);
        when(exchangeRateCache.get("EUR_ANG")).thenReturn(null);
        when(exchangeRateCache.get("EUR_USD")).thenReturn(null);
        when(exchangeRateCache.get("EUR_EUR")).thenReturn(null);

        // Verify the result is as expected
        assertThatThrownBy(() -> exchangeService.getExchangeRateForAll("EUR"))
                .isInstanceOf(ExternalApiConnectionError.class)
                .hasMessage("External API request failed");

        // Method invocation verifications
        verify(currencyRepository, times(1)).existsByCode("EUR");
        verify(currencyRepository, times(1)).findAll();

        verify(exchangeRateCache, times(4)).get(Mockito.any());
        verify(externalApiService, times(1)).getLatestExchanges("EUR", Optional.of("EUR,USD,AMD,ANG,"));
    }

    @Test
    void whenGettingExchangeRateForAll_withInvalidInput_thenThrowException() {
        // Set up Expectations
        when(currencyRepository.existsByCode("ZZZ")).thenReturn(false);

        // Verify the result is as expected
        assertThatThrownBy(() -> exchangeService.getExchangeRateForAll("ZZZ"))
                .isInstanceOf(InvalidCurrencyException.class)
                .hasMessage("Invalid currency code provided!");

    }

    @Test
    void whenGettingExchangeRateForSpecificCurrency_withValidInput_NotInCache_thenContactExternalAPI() {
        // Set up Expectations
        HashMap<String, Double> returnedRates = new HashMap<>();

        returnedRates.put("USD", 1.088186);

        ExchangeRateDTO ratesDTO = new ExchangeRateDTO();
        ratesDTO.setRates(returnedRates);

        when(externalApiService.getLatestExchanges("EUR", Optional.of("USD"))).thenReturn(ratesDTO);
        when(currencyRepository.existsByCode("EUR")).thenReturn(true);
        when(currencyRepository.existsByCode("USD")).thenReturn(true);
        when(currencyRepository.findByCode("USD")).thenReturn(Optional.of(dollar));

        // Cache calls
        when(cacheManager.getCache(Mockito.any())).thenReturn(exchangeRateCache);

        // Verify the result is as expected
        Double exchangeRate = exchangeService.getExchangeRateForSpecificCurrency("EUR", "USD");

        assertThat(exchangeRate).isEqualTo(1.088186);

        // Method invocation verifications
        verify(externalApiService, times(1)).getLatestExchanges("EUR", Optional.of("USD"));
        verify(exchangeRateCache, times(1)).put("EUR_USD", 1.088186);
    }

    @Test
    void whenGettingExchangeRateForSpecificCurrency_withValidInput_NotInCache_externalAPIFailure_thenThrowException() {
        // Set up Expectations
        when(currencyRepository.existsByCode("EUR")).thenReturn(true);
        when(currencyRepository.existsByCode("USD")).thenReturn(true);
        when(externalApiService.getLatestExchanges("EUR", Optional.of("USD"))).thenThrow(new ExternalApiConnectionError("External API request failed"));

        // Verify the result is as expected
        assertThatThrownBy(() -> exchangeService.getExchangeRateForSpecificCurrency("EUR", "USD"))
                .isInstanceOf(ExternalApiConnectionError.class)
                .hasMessage("External API request failed");

        // Method invocation verifications
        verify(externalApiService, times(1)).getLatestExchanges("EUR", Optional.of("USD"));
    }

    @Test
    void whenGettingExchangeRateForSpecificCurrency_withInvalidFromInput_thenThrowException() {
        // Set up Expectations
        when(currencyRepository.existsByCode("EUR")).thenReturn(true);
        when(currencyRepository.existsByCode("ZZZ")).thenReturn(false);

        // Verify the result is as expected
        assertThatThrownBy(() -> exchangeService.getExchangeRateForSpecificCurrency("ZZZ","EUR"))
                .isInstanceOf(InvalidCurrencyException.class)
                .hasMessage("Invalid currency code(s) provided!");
    }

    @Test
    void whenGettingExchangeRateForSpecificCurrency_withInvalidToInput_thenThrowException() {
        // Set up Expectations
        when(currencyRepository.existsByCode("EUR")).thenReturn(true);
        when(currencyRepository.existsByCode("ZZZ")).thenReturn(false);

        // Verify the result is as expected
        assertThatThrownBy(() -> exchangeService.getExchangeRateForSpecificCurrency("EUR","ZZZ"))
                .isInstanceOf(InvalidCurrencyException.class)
                .hasMessage("Invalid currency code(s) provided!");
    }
}
