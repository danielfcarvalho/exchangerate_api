package com.dfc.exchange_api.backend.unitTests;

import com.dfc.exchange_api.backend.services.ExternalApiService;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.verification.VerificationModeFactory;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ExternalApiService_unitTest {
    @Mock
    RestTemplate mockRestTemplate;
    @InjectMocks
    ExternalApiService externalApiService;

    private final String BASE_URL = "https://api.exchangerate.host";

    @Test
    void whenGetLatestExchanges_returnsSuccess() throws URISyntaxException {
        // Set up Expectations
        URI uri = new URI(BASE_URL + "/latest&base=EUR");

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
                "        \"AED\": 3.989863,\n" +
                "        \"AFN\": 91.961929,\n" +
                "        \"ALL\": 105.208474,\n" +
                "        \"AMD\": 422.228721,\n" +
                "        \"ANG\": 1.965639\n" +
                "    }\n" +
                "}";

        when(mockRestTemplate.getForObject(uri, String.class)).thenReturn(mockResponse);

        // Verify the result is as expected
        JsonObject response = externalApiService.getLatestExchanges("EUR", Optional.empty());
        assertThat(response.has("AED")).isEqualTo(true);

        // Verify that the external API was called and Verify that the cache was called twice - to query and to add the new record
        Mockito.verify(mockRestTemplate, VerificationModeFactory.times(1)).getForObject(Mockito.any(), Mockito.any());
    }

    @Test
    void whenGetLatestExchanges_withSymbols_returnsSuccess() throws URISyntaxException {
        // Set up Expectations
        URI uri = new URI(BASE_URL + "/latest&base=EUR&symbols=GBP,USD");

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
                "        \"GBP\": 0.853548,\n" +
                "        \"USD\": 1.086628\n" +
                "    }\n" +
                "}";

        when(mockRestTemplate.getForObject(uri, String.class)).thenReturn(mockResponse);

        // Verify the result is as expected
        JsonObject response = externalApiService.getLatestExchanges("EUR", Optional.of("GBP,USD"));
        assertThat(response.has("USD")).isEqualTo(true);
        assertThat(response.has("GBP")).isEqualTo(true);
        assertThat(response.has("AMD")).isEqualTo(false);

        // Verify that the external API was called and Verify that the cache was called twice - to query and to add the new record
        Mockito.verify(mockRestTemplate, VerificationModeFactory.times(1)).getForObject(Mockito.any(), Mockito.any());
    }

    @Test
    void whenGetConversionValues_returnsSuccess(){

    }
}
