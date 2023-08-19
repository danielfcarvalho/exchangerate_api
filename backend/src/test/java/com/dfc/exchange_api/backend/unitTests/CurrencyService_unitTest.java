package com.dfc.exchange_api.backend.unitTests;

import com.dfc.exchange_api.backend.exceptions.ExternalApiConnectionError;
import com.dfc.exchange_api.backend.models.Currency;
import com.dfc.exchange_api.backend.repositories.CurrencyRepository;
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
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

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


    @BeforeEach
    void setUp() {
        dirham = new Currency("United Arab Emirates Dirham", "AED");
        afghani = new Currency("Afghan Afghani", "AFN");
        lek = new Currency("Albanian Lek", "ALL");
    }

    @AfterEach
    void tearDown() {
        dirham = null;
        afghani = null;
        lek = null;
    }

    @Test
    void testFetchSupportedCurrencies_repositoryEmpty() {
        // Setting up Expectations
        String mockResponse = "{\n" +
                "    \"motd\": {\n" +
                "        \"msg\": \"If you or your company use this project or like what we doing, please consider backing us so we can continue maintaining and evolving this project.\",\n" +
                "        \"url\": \"https://exchangerate.host/#/donate\"\n" +
                "    },\n" +
                "    \"success\": true,\n" +
                "    \"symbols\": {\n" +
                "        \"AED\": {\n" +
                "            \"description\": \"United Arab Emirates Dirham\",\n" +
                "            \"code\": \"AED\"\n" +
                "        },\n" +
                "        \"AFN\": {\n" +
                "            \"description\": \"Afghan Afghani\",\n" +
                "            \"code\": \"AFN\"\n" +
                "        },\n" +
                "        \"ALL\": {\n" +
                "            \"description\": \"Albanian Lek\",\n" +
                "            \"code\": \"ALL\"\n" +
                "        }\n" +
                "    }\n" +
                "}";

        JsonElement symbols = JsonParser.parseString(mockResponse).getAsJsonObject().get("symbols");

        when(externalApiService.getAvailableCurrencies()).thenReturn(symbols);
        when(currencyRepository.findByCode(anyString())).thenReturn(Optional.empty());

        // Call the method under test
        currencyService.fetchSupportedCurrencies();

        // Verify that the repository's saveAll method was called
        verify(currencyRepository).saveAll(anyList());
    }

    @Test
    void testFetchSupportedCurrencies_repositoryNotEmpty() {
        // Setting up Expectations
        String mockResponse = "{\n" +
                "    \"motd\": {\n" +
                "        \"msg\": \"If you or your company use this project or like what we doing, please consider backing us so we can continue maintaining and evolving this project.\",\n" +
                "        \"url\": \"https://exchangerate.host/#/donate\"\n" +
                "    },\n" +
                "    \"success\": true,\n" +
                "    \"symbols\": {\n" +
                "        \"AED\": {\n" +
                "            \"description\": \"United Arab Emirates Dirham\",\n" +
                "            \"code\": \"AED\"\n" +
                "        },\n" +
                "        \"AFN\": {\n" +
                "            \"description\": \"Afghan Afghani\",\n" +
                "            \"code\": \"AFN\"\n" +
                "        },\n" +
                "        \"ALL\": {\n" +
                "            \"description\": \"Albanian Lek\",\n" +
                "            \"code\": \"ALL\"\n" +
                "        }\n" +
                "    }\n" +
                "}";

        JsonElement symbols = JsonParser.parseString(mockResponse).getAsJsonObject().get("symbols");

        when(externalApiService.getAvailableCurrencies()).thenReturn(symbols);
        when(currencyRepository.findByCode("AED")).thenReturn(Optional.of(dirham));
        when(currencyRepository.findByCode("AFN")).thenReturn(Optional.of(afghani));
        when(currencyRepository.findByCode("ALL")).thenReturn(Optional.of(lek));

        // Call the method under test
        currencyService.fetchSupportedCurrencies();

        // Verify that the repository's saveAll method was called
        verify(currencyRepository, never()).saveAll(anyList());
    }
}
