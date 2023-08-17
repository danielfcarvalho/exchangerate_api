package com.dfc.exchange_api.backend.services;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.logging.Logger;

@Service
public class ExternalApiService {
    private final RestTemplate restTemplate;
    private final String BASE_URL = "https://api.exchangerate.host";
    private final static Logger LOGGER =  Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    public ExternalApiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public JsonObject getLatestExchanges(String base, Optional<String> symbols) throws URISyntaxException {
        // Calling the External API
        URI uri;

        if(symbols.isPresent()){
            uri = new URI(BASE_URL + "/latest&base=" + base + "&symbols=" + symbols.get());
        }else{
            uri = new URI(BASE_URL + "/latest&base=" + base);
        }

        LOGGER.info("Calling the Exchange Rate API on the following path: " + uri);
        String response = restTemplate.getForObject(uri, String.class);
        LOGGER.info("Full response as String " + response);

        JsonObject obj = JsonParser.parseString(response).getAsJsonObject().getAsJsonObject("rates");
        LOGGER.info("Extracted response as JSON " + obj);

        return obj;
    }

    public JsonObject getConversionValues(String from, String to, Double amount) throws URISyntaxException {
        // Calling the External API
        URI uri;
        uri = new URI(BASE_URL + "/convert&from=" + from + "&to=" + to + "&amount=" + amount);

        LOGGER.info("Calling the Exchange Rate API on the following path: " + uri);
        String response = restTemplate.getForObject(uri, String.class);
        LOGGER.info("Full response as String " + response);

        JsonObject obj = JsonParser.parseString(response).getAsJsonObject().getAsJsonObject("result");
        LOGGER.info("Extracted response as JSON " + obj);

        return obj;
    }

}
