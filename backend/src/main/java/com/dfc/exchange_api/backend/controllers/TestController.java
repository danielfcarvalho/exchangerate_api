package com.dfc.exchange_api.backend.controllers;

import com.dfc.exchange_api.backend.services.ExternalApiService;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.net.URISyntaxException;


@RestController
public class TestController {

    @Autowired
    ExternalApiService externalApiService;

    @GetMapping("latest")
    public ResponseEntity<JsonObject> getLatestsExchange() throws URISyntaxException {

        return ResponseEntity.ok().body(externalApiService.getAvailableCurrencies());
    }
}
