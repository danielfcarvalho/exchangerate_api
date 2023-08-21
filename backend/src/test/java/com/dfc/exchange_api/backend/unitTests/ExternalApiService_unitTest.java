package com.dfc.exchange_api.backend.unitTests;

import com.dfc.exchange_api.backend.exceptions.ExternalApiConnectionError;
import com.dfc.exchange_api.backend.services.ExternalApiService;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.verification.VerificationModeFactory;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Optional;

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
        JsonObject response = externalApiService.getLatestExchanges("EUR", Optional.empty()).getAsJsonObject();
        assertThat(response.has("AED")).isTrue();
        assertThat(response.has("motd")).isFalse();

        // Verify that the external API was called and Verify that the cache was called twice - to query and to add the new record
        Mockito.verify(mockRestTemplate, VerificationModeFactory.times(1)).getForObject(Mockito.any(), Mockito.any());
    }

    @Test
    void whenGetLatestExchanges_withSymbols_returnsSuccess() {
        // Set up Expectations
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(BASE_URL).path("/latest")
                .queryParam("base", "EUR").queryParam("symbols", "GBP,USD");

        URI uri = uriBuilder.build().toUri();

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
        JsonObject response = externalApiService.getLatestExchanges("EUR", Optional.of("GBP,USD")).getAsJsonObject();
        assertThat(response.has("USD")).isTrue();
        assertThat(response.has("GBP")).isTrue();
        assertThat(response.has("AMD")).isFalse();
        assertThat(response.has("motd")).isFalse();

        // Verify that the external API was called and Verify that the cache was called twice - to query and to add the new record
        Mockito.verify(mockRestTemplate, VerificationModeFactory.times(1)).getForObject(Mockito.any(), Mockito.any());
    }

    @Test
    void whenGetLatestExchanges_returnsBadRequest_thenThrowException() {
        // Set up Expectations
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(BASE_URL).path("/latest")
                .queryParam("base", "EUR");

        URI uri = uriBuilder.build().toUri();

        MockRestServiceServer mockServer = MockRestServiceServer.createServer(mockRestTemplate);

        mockServer.expect(requestTo(uri))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"error\":\"Bad request\"}"));



        // Verify the result is as expected
        assertThatThrownBy(() -> externalApiService.getLatestExchanges("EUR", Optional.empty())).isInstanceOf(ExternalApiConnectionError.class);
    }
    
    @Test
    void whenGetAvailableCurrencies_returnsSuccess() {
        // Set up Expectations
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(BASE_URL).path("/symbols");
        URI uri = uriBuilder.build().toUri();

        String mockResponse = "{" +
                "\"motd\": {" +
                "\"msg\": \"If you or your company use this project or like what we doing, please consider backing us so we can continue maintaining and evolving this project.\"," +
                "\"url\": \"https://exchangerate.host/#/donate\"" +
                "}," +
                "\"success\": true," +
                "\"symbols\": {" +
                "\"AED\": {" +
                "\"description\": \"United Arab Emirates Dirham\"," +
                "\"code\": \"AED\"" +
                "}," +
                "\"AFN\": {" +
                "\"description\": \"Afghan Afghani\"," +
                "\"code\": \"AFN\"" +
                "}," +
                "\"ZAR\": {" +
                "\"description\": \"South African Rand\"," +
                "\"code\": \"ZAR\"" +
                "}," +
                "\"ZMW\": {" +
                "\"description\": \"Zambian Kwacha\"," +
                "\"code\": \"ZMW\"" +
                "}," +
                "\"ZWL\": {" +
                "\"description\": \"Zimbabwean Dollar\"," +
                "\"code\": \"ZWL\"" +
                "}" +
                "}" +
                "}";

        when(mockRestTemplate.getForObject(uri, String.class)).thenReturn(mockResponse);

        // Verify the result is as expected
        JsonObject response = externalApiService.getAvailableCurrencies().getAsJsonObject();
        assertThat(response.has("AED")).isTrue();
        assertThat(response.has("ZWL")).isTrue();
        assertThat(response.has("motd")).isFalse();

        // Verify that the external API was called and Verify that the cache was called twice - to query and to add the new record
        Mockito.verify(mockRestTemplate, VerificationModeFactory.times(1)).getForObject(Mockito.any(), Mockito.any());
    }
}
