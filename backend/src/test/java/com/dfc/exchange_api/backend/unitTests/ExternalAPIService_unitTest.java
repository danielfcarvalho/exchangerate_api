package com.dfc.exchange_api.backend.unitTests;

import com.dfc.exchange_api.backend.models.ExchangeRateDTO;
import com.dfc.exchange_api.backend.models.FetchedSymbolsDTO;
import com.dfc.exchange_api.backend.services.ExternalApiService;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ExternalAPIService_unitTest {
    private MockWebServer mockWebServer;

    @InjectMocks
    ExternalApiService externalApiService;

    @BeforeEach
    void setup() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        externalApiService.setBASE_URL( mockWebServer.url("/").toString());
    }

    @AfterEach
    void teardown() throws IOException {
        mockWebServer.shutdown();
    }
    // Testing the connection to the exchange endpoint

    @Test
    void whenGetLatestExchanges_returnsSuccess() {
        // Set up Expectations
        String serverResponse = "{\n" +
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

        MockResponse mockResponse = new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(serverResponse);
        mockWebServer.enqueue(mockResponse);

        // Verify the result is as expected
        ExchangeRateDTO response = externalApiService.getLatestExchanges("EUR", Optional.empty());
        assertThat(response.getRates()).containsOnlyKeys("AED", "AFN", "ALL", "AMD", "ANG");

    }

    @Test
    void whenGetLatestExchanges_withSymbols_returnsSuccess() {
        // Set up Expectations
        String serverResponse = "{\n" +
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

        MockResponse mockResponse = new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(serverResponse);
        mockWebServer.enqueue(mockResponse);


        // Verify the result is as expected
        ExchangeRateDTO response = externalApiService.getLatestExchanges("EUR", Optional.empty());
        assertThat(response.getRates()).containsOnlyKeys("GBP", "USD");
    }

    @Test
    void whenGetAvailableCurrencies_returnsSuccess() {
        // Set up Expectations
        String serverResponse = "{" +
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

        MockResponse mockResponse = new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(serverResponse);
        mockWebServer.enqueue(mockResponse);

        // Verify the result is as expected
        FetchedSymbolsDTO response = externalApiService.getAvailableCurrencies();
        assertThat(response.getSymbols()).containsOnlyKeys("AED", "AFN", "ZAR", "ZMW", "ZWL");
    }

}