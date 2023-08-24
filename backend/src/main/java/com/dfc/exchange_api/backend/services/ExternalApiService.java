package com.dfc.exchange_api.backend.services;

import com.dfc.exchange_api.backend.exceptions.ExternalApiConnectionError;
import com.dfc.exchange_api.backend.models.ExchangeRateDTO;
import com.dfc.exchange_api.backend.models.FetchedSymbolsDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

/**
 * Service that handles the connection to the external Exchange Rate API
 */
@Service
public class ExternalApiService {
    private String BASE_URL = "https://api.exchangerate.host";
    private static final Logger LOGGER = LoggerFactory.getLogger(ExternalApiService.class);
    private final WebClient webClient;

    public ExternalApiService() {
        this.webClient = WebClient.builder()
                .baseUrl(BASE_URL)
                .defaultCookie("cookieKey", "cookieValue")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    /**
     * This method contacts the /latest endpoint in the Exchange Rate API, which fetches the latest conversion rates from
     * a Currency passed as parameter. It can return either the list of conversion rates to all the supported currencies,
     * or to a subset passed as a request parameter.
     * In case the External API doesn't reply with an HTTP STATUS OK message, either a customized exception is thrown, or
     * the endpoint is contacted again using @Retryable, in the case of a TIMEOUT.
     * @param base - The currency for which the conversion rates will be fetched.
     * @param symbols - Optional subset of currencies to get the corresponding covnersion rates from.
     * @return The DTO entity representing the fetched exchange rates
     * @throws ExternalApiConnectionError - in case of an error in the connection to the External API
     */
    public ExchangeRateDTO getLatestExchanges(String base, Optional<String> symbols) throws ExternalApiConnectionError {
        // Calling the External API
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(BASE_URL).path("/latest")
                .queryParam("base", base);

        symbols.ifPresent(s -> uriBuilder.queryParam("symbols", s));
        URI uri = uriBuilder.build().toUri();

        // Calling the endpoint and fetching the required response
        return this.doHttpGet(uri, ExchangeRateDTO.class);
    }

    /**
     * This method contacts the /symbols endpoint in the external API, retrieving the full list of supported currencies
     * by said API.
     * @return The DTO entity representing the fetched currencies
     * @throws ExternalApiConnectionError - in case of an error in the connection to the External API
     */
    public FetchedSymbolsDTO getAvailableCurrencies() throws ExternalApiConnectionError {
        // Calling the External API
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(BASE_URL).path("/symbols");
        URI uri = uriBuilder.build().toUri();

        // Calling the endpoint and fetching the required response
        return this.doHttpGet(uri, FetchedSymbolsDTO.class);
    }

    /**
     * Auxiliary method that will contact the required endpoint using restTemplate, and return the expected DTO response
     * to the calling method.
     * In case the External API doesn't reply with an HTTP STATUS OK message, either a customized exception, ExternalApiConnectionError,
     * is thrown, or the endpoint is contacted again using @Retryable, in the case of a TIMEOUT.
     * @param uri - The URI path of the External API endpoint to be called
     * @param responseType - The class of the expected DTO containing the unpacked response
     * @return the DTO class containing the response from the server
     * @throws ExternalApiConnectionError - in case of an error in the connection to the External API
     */
    @Retryable(value = { TimeoutException.class }, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    private <T> T doHttpGet(URI uri, Class<T> responseType) throws ExternalApiConnectionError {
        try{
            LOGGER.info("Calling the Exchange Rate API on the following path: {}", uri);
            T response = webClient.get()
                            .uri(uri)
                                    .retrieve()
                                            .bodyToMono(responseType)
                                                    .block();
            LOGGER.info("Full response as String: {}", response);

            return response;
        } catch (HttpServerErrorException ex) {
            // Handle 5xx server errors
            LOGGER.error("External API server error: {}", ex.getMessage());
            throw new ExternalApiConnectionError("External API server error");
        } catch (HttpClientErrorException ex) {
            // Handle 4xx client errors
            LOGGER.error("External API client error: {}", ex.getMessage());
            throw new ExternalApiConnectionError("External API client error");
        } catch (RestClientException|NullPointerException ex) {
            // Handle other exceptions (e.g., connection errors)
            LOGGER.error("External API request failed: {}", ex.getMessage());
            throw new ExternalApiConnectionError("External API request failed");
        }
    }
}
