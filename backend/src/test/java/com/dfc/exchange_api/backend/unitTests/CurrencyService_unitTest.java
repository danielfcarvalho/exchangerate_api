package com.dfc.exchange_api.backend.unitTests;

import com.dfc.exchange_api.backend.models.Currency;
import com.dfc.exchange_api.backend.models.CurrencyDTO;
import com.dfc.exchange_api.backend.models.FetchedSymbolsDTO;
import com.dfc.exchange_api.backend.repositories.CurrencyRepository;
import com.dfc.exchange_api.backend.services.CurrencyService;
import com.dfc.exchange_api.backend.services.ExternalApiService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CurrencyService_unitTest {
    @Mock
    private ExternalApiService externalApiService;

    @Mock(lenient = true)
    private CurrencyRepository currencyRepository;

    @InjectMocks
    private CurrencyService currencyService;

    Currency dirham;
    Currency afghani;
    Currency lek;
    Currency euro;


    @BeforeEach
    void setUp() {
        dirham = new Currency("United Arab Emirates Dirham", "AED");
        afghani = new Currency("Afghan Afghani", "AFN");
        lek = new Currency("Albanian Lek", "ALL");
        euro = new Currency("Euro", "EUR");
    }

    @AfterEach
    void tearDown() {
        dirham = null;
        afghani = null;
        lek = null;
        euro = null;
    }

    @Test
    void testFetchSupportedCurrencies_repositoryEmpty() {
        // Setting up Expectations
        TreeMap<String, CurrencyDTO> fetchedCurrencies = new TreeMap<>();

        fetchedCurrencies.put("AED", new CurrencyDTO("United Arab Emirates Dirham", "AED"));
        fetchedCurrencies.put("ALL", new CurrencyDTO("Albanian Lek", "ALL"));

        FetchedSymbolsDTO fetchedSymbols = new FetchedSymbolsDTO();
        fetchedSymbols.setSymbols(fetchedCurrencies);

        when(externalApiService.getAvailableCurrencies()).thenReturn(fetchedSymbols);
        when(currencyRepository.findByCode(anyString())).thenReturn(Optional.empty());
        when(currencyRepository.findAll()).thenReturn(Collections.emptyList());

        // Call the method under test
        currencyService.fetchSupportedCurrencies();

        // Verify that the repository's saveAll method was called
        verify(currencyRepository).saveAll(anyList());
    }

    @Test
    void testFetchSupportedCurrencies_repositoryNotEmpty() {
        // Setting up Expectations
        TreeMap<String, CurrencyDTO> fetchedCurrencies = new TreeMap<>();

        fetchedCurrencies.put("AED", new CurrencyDTO("United Arab Emirates Dirham", "AED"));
        fetchedCurrencies.put("AFN", new CurrencyDTO("Afghan Afghani", "AFN"));
        fetchedCurrencies.put("ALL", new CurrencyDTO("Albanian Lek", "ALL"));

        FetchedSymbolsDTO fetchedSymbols = new FetchedSymbolsDTO();
        fetchedSymbols.setSymbols(fetchedCurrencies);

        List<Currency> existingCurrencies = List.of(dirham, afghani, lek);

        when(externalApiService.getAvailableCurrencies()).thenReturn(fetchedSymbols);
        when(currencyRepository.findByCode("AED")).thenReturn(Optional.of(dirham));
        when(currencyRepository.findByCode("AFN")).thenReturn(Optional.of(afghani));
        when(currencyRepository.findByCode("ALL")).thenReturn(Optional.of(lek));
        when(currencyRepository.findAll()).thenReturn(existingCurrencies);

        // Call the method under test
        currencyService.fetchSupportedCurrencies();

        // Verify that the repository's saveAll method was called
        verify(currencyRepository, never()).saveAll(anyList());
    }

    @Test
    void testFetchSupportedCurrencies_repositoryNotEmpty_hasToDeleteOutdatedCurrencies() {
        // Setting up Expectations
        TreeMap<String, CurrencyDTO> fetchedCurrencies = new TreeMap<>();

        fetchedCurrencies.put("AED", new CurrencyDTO("United Arab Emirates Dirham", "AED"));
        fetchedCurrencies.put("AFN", new CurrencyDTO("Afghan Afghani", "AFN"));
        fetchedCurrencies.put("ALL", new CurrencyDTO("Albanian Lek", "ALL"));

        FetchedSymbolsDTO fetchedSymbols = new FetchedSymbolsDTO();
        fetchedSymbols.setSymbols(fetchedCurrencies);

        List<Currency> existingCurrencies = List.of(dirham, afghani, euro, lek);

        when(externalApiService.getAvailableCurrencies()).thenReturn(fetchedSymbols);
        when(currencyRepository.findByCode("AED")).thenReturn(Optional.of(dirham));
        when(currencyRepository.findByCode("AFN")).thenReturn(Optional.of(afghani));
        when(currencyRepository.findByCode("ALL")).thenReturn(Optional.of(lek));
        when(currencyRepository.findAll()).thenReturn(existingCurrencies);

        // Call the method under test
        currencyService.fetchSupportedCurrencies();

        // Verify that the repository's saveAll method was never called, and that the delete method was called for no longer supported "EUR"
        verify(currencyRepository, never()).saveAll(anyList());
        verify(currencyRepository).deleteAll(List.of(euro));
    }

    @Test
    void getSupportedCurrencies_withRepositoryEmpty(){
        // Setting up Expectations
        when(currencyRepository.findAll()).thenReturn(Collections.emptyList());

        // Call the method under test
        assertThat(currencyService.getSupportedCurrencies()).isEmpty();

        // Verify that the repository's saveAll method was called
        verify(currencyRepository).findAll();
    }

    @Test
    void getSupportedCurrencies_withRepositoryFull(){
        // Setting up Expectations
        List<Currency> currenciesOnRepo = Arrays.asList(dirham, afghani, lek, euro);
        when(currencyRepository.findAll()).thenReturn(currenciesOnRepo);

        // Call the method under test
        assertThat(currencyService.getSupportedCurrencies()).isEqualTo(currenciesOnRepo);

        // Verify that the repository's saveAll method was called
        verify(currencyRepository).findAll();
    }
}
