package com.dfc.exchange_api.backend.unitTests;

import com.dfc.exchange_api.backend.models.CurrencyDTO;
import com.dfc.exchange_api.backend.models.ExchangeRateDTO;
import com.dfc.exchange_api.backend.models.FetchedSymbolsDTO;
import com.dfc.exchange_api.backend.services.ExternalApiService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.verification.VerificationModeFactory;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.HashMap;
import java.util.Optional;
import java.util.TreeMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

@ExtendWith(MockitoExtension.class)
class ExternalApiService_unitTest {
    @Mock
    RestTemplate mockRestTemplate;
    @InjectMocks
    ExternalApiService externalApiService;

    private final String BASE_URL = "https://api.exchangerate.host";

    // Testing the connection to the exchange endpoint

    @Test
    void whenGetLatestExchanges_returnsSuccess() {
        // Set up Expectations
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(BASE_URL).path("/latest")
                .queryParam("base", "EUR");

        URI uri = uriBuilder.build().toUri();

        HashMap<String, Double> returnedRates = new HashMap<>();

        returnedRates.put("AED", 3.989863);
        returnedRates.put("AFN", 91.961929);
        returnedRates.put("ALL", 105.208474);
        returnedRates.put("AMD", 422.228721);
        returnedRates.put("ANG", 1.965639);

        ExchangeRateDTO ratesDTO = new ExchangeRateDTO();
        ratesDTO.setRates(returnedRates);

        when(mockRestTemplate.getForObject(uri, ExchangeRateDTO.class)).thenReturn(ratesDTO);

        // Verify the result is as expected
        ExchangeRateDTO response = externalApiService.getLatestExchanges("EUR", Optional.empty());
        assertThat(response.getRates()).isEqualTo(returnedRates);

        // Verify that the external API was called and Verify that the cache was called twice - to query and to add the new record
        Mockito.verify(mockRestTemplate, VerificationModeFactory.times(1)).getForObject(Mockito.any(), Mockito.any());
    }

    @Test
    void whenGetLatestExchanges_withSymbols_returnsSuccess() {
        // Set up Expectations
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(BASE_URL).path("/latest")
                .queryParam("base", "EUR").queryParam("symbols", "GBP,USD");

        URI uri = uriBuilder.build().toUri();

        HashMap<String, Double> returnedRates = new HashMap<>();

        returnedRates.put("GBP", 0.853548);
        returnedRates.put("USD", 1.086628);

        ExchangeRateDTO ratesDTO = new ExchangeRateDTO();
        ratesDTO.setRates(returnedRates);

        when(mockRestTemplate.getForObject(uri, ExchangeRateDTO.class)).thenReturn(ratesDTO);

        // Verify the result is as expected
        ExchangeRateDTO response = externalApiService.getLatestExchanges("EUR", Optional.of("GBP,USD"));
        assertThat(response.getRates()).isEqualTo(returnedRates);

        // Verify that the external API was called and Verify that the cache was called twice - to query and to add the new record
        Mockito.verify(mockRestTemplate, VerificationModeFactory.times(1)).getForObject(Mockito.any(), Mockito.any());
    }

    @Test
    void whenGetAvailableCurrencies_returnsSuccess() {
        // Set up Expectations
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(BASE_URL).path("/symbols");
        URI uri = uriBuilder.build().toUri();

        TreeMap<String, CurrencyDTO> fetchedCurrencies = new TreeMap<>();

        fetchedCurrencies.put("AED", new CurrencyDTO("United Arab Emirates Dirham", "AED"));
        fetchedCurrencies.put("AED", new CurrencyDTO("Zimbabwean Dollar", "ZWL"));

        FetchedSymbolsDTO fetchedSymbols = new FetchedSymbolsDTO();
        fetchedSymbols.setSymbols(fetchedCurrencies);

        when(mockRestTemplate.getForObject(uri, FetchedSymbolsDTO.class)).thenReturn(fetchedSymbols);

        // Verify the result is as expected
        FetchedSymbolsDTO response = externalApiService.getAvailableCurrencies();
        assertThat(response.getSymbols()).isEqualTo(fetchedCurrencies);

        // Verify that the external API was called and Verify that the cache was called twice - to query and to add the new record
        Mockito.verify(mockRestTemplate, VerificationModeFactory.times(1)).getForObject(Mockito.any(), Mockito.any());
    }
}
