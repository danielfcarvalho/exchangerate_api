package com.dfc.exchange_api.backend.services;

import com.dfc.exchange_api.backend.exceptions.ExternalApiConnectionError;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

@Service
public class ExternalApiService {
    private final RestTemplate restTemplate;
    private final String BASE_URL = "https://api.exchangerate.host";
    private static final Logger LOGGER = LoggerFactory.getLogger(ExternalApiService.class);

    public ExternalApiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Retryable(value = { TimeoutException.class }, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public JsonObject getLatestExchanges(String base, Optional<String> symbols) throws URISyntaxException {
        // Calling the External API
        URI uri;

        if(symbols.isPresent()){
            uri = new URI(BASE_URL + "/latest&base=" + base + "&symbols=" + symbols.get());
        }else{
            uri = new URI(BASE_URL + "/latest&base=" + base);
        }

        try {
            LOGGER.info("Calling the Exchange Rate API on the following path: " + uri);
            String response = restTemplate.getForObject(uri, String.class);
            LOGGER.info("Full response as String " + response);

            JsonObject obj = JsonParser.parseString(response).getAsJsonObject().getAsJsonObject("rates");
            LOGGER.info("Extracted response as JSON " + obj);

            return obj;
        } catch (HttpServerErrorException ex) {
            // Handle 5xx server errors
            LOGGER.error("External API server error: {}", ex.getMessage());
            throw new ExternalApiConnectionError("External API server error");
        } catch (HttpClientErrorException ex) {
            // Handle 4xx client errors
            LOGGER.error("External API client error: {}", ex.getMessage());
            throw new ExternalApiConnectionError("External API client error");
        } catch (RestClientException ex) {
            // Handle other exceptions (e.g., connection errors)
            LOGGER.error("External API request failed: {}", ex.getMessage());
            throw new ExternalApiConnectionError("External API request failed");
        } catch (NullPointerException ex) {
            // Expected response or field is not present
            LOGGER.error("External API request failed: {}", ex.getMessage());
            throw new ExternalApiConnectionError("External API request failed");
        }
    }

    public JsonPrimitive getConversionValues(String from, String to, Double amount) throws URISyntaxException {
        // Calling the External API
        URI uri;
        uri = new URI(BASE_URL + "/convert&from=" + from + "&to=" + to + "&amount=" + amount);

        try{
            LOGGER.info("Calling the Exchange Rate API on the following path: " + uri);
            String response = restTemplate.getForObject(uri, String.class);
            LOGGER.info("Full response as String " + response);

            JsonPrimitive obj = JsonParser.parseString(response).getAsJsonObject().getAsJsonPrimitive("result");
            LOGGER.info("Extracted response as JSON " + obj);

            return obj;
        } catch (HttpServerErrorException ex) {
            // Handle 5xx server errors
            LOGGER.error("External API server error: {}", ex.getMessage());
            throw new ExternalApiConnectionError("External API server error");
        } catch (HttpClientErrorException ex) {
            // Handle 4xx client errors
            LOGGER.error("External API client error: {}", ex.getMessage());
            throw new ExternalApiConnectionError("External API client error");
        } catch (RestClientException ex) {
            // Handle other exceptions (e.g., connection errors)
            LOGGER.error("External API request failed: {}", ex.getMessage());
            throw new ExternalApiConnectionError("External API request failed");
        } catch (NullPointerException ex) {
            // Expected response or field is not present
            LOGGER.error("External API request failed: {}", ex.getMessage());
            throw new ExternalApiConnectionError("External API request failed");
        }
    }
}
