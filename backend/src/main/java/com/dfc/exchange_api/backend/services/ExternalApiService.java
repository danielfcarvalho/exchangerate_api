package com.dfc.exchange_api.backend.services;

import com.dfc.exchange_api.backend.exceptions.ExternalApiConnectionError;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
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
import java.util.Optional;
import java.util.concurrent.TimeoutException;

/**
 * Service that handles the connection to the external Exchange Rate API
 */
@Service
public class ExternalApiService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExternalApiService.class);
    private final RestTemplate restTemplate;
    private final String BASE_URL = "https://api.exchangerate.host";

    public ExternalApiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * This method contacts the /latest endpoint in the Exchange Rate API, which fetches the latest conversion rates from
     * a Currency passed as parameter. It can return either the list of conversion rates to all the supported currencies,
     * or to a subset passed as a request parameter.
     * In this case, the method will parse the fetched JSON response and return a JsonElement to be used by the other service
     * methods that depend on this endpoint.
     * In case the External API doesn't reply with an HTTP STATUS OK message, either a customized exception is thrown, or
     * the endpoint is contacted again using @Retryable, in the case of a TIMEOUT.
     * @param base - The currency for which the conversion rates will be fetched.
     * @param symbols - Optional subset of currencies to get the corresponding covnersion rates from.
     * @return a JSON Object with the conversion rates
     * @throws ExternalApiConnectionError
     */
    public JsonElement getLatestExchanges(String base, Optional<String> symbols) throws ExternalApiConnectionError {
        // Calling the External API
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(BASE_URL).path("/latest")
                .queryParam("base", base);

        symbols.ifPresent(s -> uriBuilder.queryParam("symbols", s));
        URI uri = uriBuilder.build().toUri();

        // Calling the endpoint and fetching the required JsonElement
        return this.doHttpGet(uri, "rates", false);
    }

    /**
     * This method contacts the /conversion endpoint in the Exchange Rate API, which converts a value from currency A
     * to currency B using the latest conversion rate between both.
     * In this case, the method will parse the fetched JSON response and return a JsonElement to be used by the other service
     * methods that depend on this endpoint.
     * In case the External API doesn't reply with an HTTP STATUS OK message, either a customized exception is thrown, or
     * the endpoint is contacted again using @Retryable, in the case of a TIMEOUT.
     * @param from - the Currency from which the converted value is taken from
     * @param to - the Currency to which the conversion is made
     * @param amount - quantity to be converted
     * @return a JSON Primitive with the value of the conversion
     * @throws ExternalApiConnectionError
     */
    public JsonElement getConversionValues(String from, String to, Double amount) throws ExternalApiConnectionError {
        // Calling the External API
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(BASE_URL).path("/convert")
                .queryParam("from", from).queryParam("to", to).queryParam("amount", amount);
        URI uri = uriBuilder.build().toUri();

        // Calling the endpoint and fetching the required JsonElement
        return this.doHttpGet(uri, "result", true);
    }

    /**
     * This method contacts the /symbols endpoint in the external API, retrieving the full list of supported currencies
     * by said API.
     * @return JsonArray, in which each element holds the description and code for each currency.
     *  @throws ExternalApiConnectionError
     */
    public JsonElement getAvailableCurrencies() throws ExternalApiConnectionError {
        // Calling the External API
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(BASE_URL).path("/symbols");
        URI uri = uriBuilder.build().toUri();

        // Calling the endpoint and fetching the required JsonElement
        return this.doHttpGet(uri, "symbols", false);
    }

    /**
     * Auxilliary method that will contact the required endpoint using restTemplate, and return the expected JsonElement
     * to the calling method, after parsing it using Gson's JsonParser.
     * In case the External API doesn't reply with an HTTP STATUS OK message, either a customized exception, ExternalApiConnectionError,
     * is thrown, or the endpoint is contacted again using @Retryable, in the case of a TIMEOUT.
     * @param uri - The URI path of the External API endpoint to be called
     * @param memberName - The name of the member of the JSON Response that the calling method wants to see being returned.
     *                   It will be fetched using Gson's JSON Parser
     * @param isJsonPrimitive - A boolean that checks whether the Json Element to be returned from the parsing is a JsonPrimitive, or a JsonObject.
     *                        This is because the use of JsonParser requires the extraction to be made either using a getAsJsonPrimitive() method,
     *                        or a getAsJsonObject() method, and JsonPrimitive elements cannot be casted to JsonObject.
     * @return the JsonElement that is of interest to the calling method.
     */
    @Retryable(value = { TimeoutException.class }, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    private JsonElement doHttpGet(URI uri, String memberName, boolean isJsonPrimitive) throws ExternalApiConnectionError {
        try{
            LOGGER.info("Calling the Exchange Rate API on the following path: " + uri);
            String response = restTemplate.getForObject(uri, String.class);
            LOGGER.info("Full response as String: {}", response);

            JsonElement obj;
            if(isJsonPrimitive){
                // extract as JsonPrimitive
                obj = JsonParser.parseString(response).getAsJsonObject().getAsJsonPrimitive(memberName);
            }else{
                // extract as JsonObject
                obj = JsonParser.parseString(response).getAsJsonObject().getAsJsonObject(memberName);
            }

            LOGGER.info("Extracted response as JSON: {}", obj);

            return obj;
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
