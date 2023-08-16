package com.dfc.exchange_api.backend.services;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.tomcat.util.json.JSONParser;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.logging.Logger;

@Service
public class ExternalApiService {
    private final RestTemplate restTemplate;
    private final String BASE_URL = "https://api.exchangerate.host/";
    private final static Logger LOGGER =  Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    public ExternalApiService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    public JsonObject getLatestExchanges(String base, Optional<String> symbols) throws URISyntaxException {
        // Calling the External API
        URI uri;

        if(symbols.isPresent()){
            uri = new URI(BASE_URL + "/latest&base=" + base + "&symbols=" + symbols);
        }else{
            uri = new URI(BASE_URL + "/latest&base=" + base);
        }

        LOGGER.info("Calling the Exchange Rate API on the following path: " + uri);
        String response = restTemplate.getForObject(uri, String.class);
        LOGGER.info("Response " + response);

        JsonObject obj = JsonParser.parseString(response).getAsJsonObject();
        LOGGER.info("Response JSON " + obj.getAsJsonObject("rates"));
        return obj.getAsJsonObject("rates");
    }

    public String getConversionValues(String from, String to, Double amount) throws URISyntaxException {
        // Calling the External API
        URI uri;
        uri = new URI(BASE_URL + "/convert&from=" + from + "&to=" + to + "&amount=" + amount);


        LOGGER.info("Calling the Exchange Rate API on the following path: " + uri);
        String response = restTemplate.getForObject(uri, String.class);
        LOGGER.info("Response " + response);
        return response;
    }


}
