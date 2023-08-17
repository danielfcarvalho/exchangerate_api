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
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

/**
 * Service that handles the connection to the external Exchange Rate API
 */
@Service
public class ExternalApiService {
    private final RestTemplate restTemplate;
    private final String BASE_URL = "https://api.exchangerate.host";
    private static final Logger LOGGER = LoggerFactory.getLogger(ExternalApiService.class);

    public ExternalApiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * This method contacts the /latest endpoint in the Exchange Rate API, which fetches the latest conversion rates from
     * a Currency passed as parameter. It can return either the list of conversion rates to all the supported currencies,
     * or to a subset passed as a request parameter.
     * In this case, the method will parse the fetched JSON response and return a JsonObject to be used by the other service
     * methods that depend on this endpoint.
     * In case the External API doesn't reply with an HTTP STATUS OK message, either a customized exception is thrown, or
     * the endpoint is contacted again using @Retryable, in the case of a TIMEOUT.
     * @param base - The currency for which the conversion rates will be fetched.
     * @param symbols - Optional subset of currencies to get the corresponding covnersion rates from.
     * @return a JSON Object with the conversion rates
     * @throws URISyntaxException
     */
    @Retryable(value = { TimeoutException.class }, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public JsonObject getLatestExchanges(String base, Optional<String> symbols) throws URISyntaxException {
        // Calling the External API
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(BASE_URL).path("/latest")
                .queryParam("base", base);

        symbols.ifPresent(s -> uriBuilder.queryParam("symbols", s));

        URI uri = uriBuilder.build().toUri();

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

    /**
     * This method contacts the /conversion endpoint in the Exchange Rate API, which converts a value from currency A
     * to currency B using the latest conversion rate between both.
     * In this case, the method will parse the fetched JSON response and return a JsonObject to be used by the other service
     * methods that depend on this endpoint.
     * In case the External API doesn't reply with an HTTP STATUS OK message, either a customized exception is thrown, or
     * the endpoint is contacted again using @Retryable, in the case of a TIMEOUT.
     * @param from - the Currency from which the converted value is taken from
     * @param to - the Currency to which the conversion is made
     * @param amount - quantity to be converted
     * @return a JSON Primitive with the value of the conversion
     * @throws URISyntaxException
     */
    @Retryable(value = { TimeoutException.class }, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public JsonPrimitive getConversionValues(String from, String to, Double amount) throws URISyntaxException {
        // Calling the External API
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(BASE_URL).path("/convert")
                .queryParam("from", from).queryParam("to", to).queryParam("amount", amount);

        URI uri = uriBuilder.build().toUri();

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
