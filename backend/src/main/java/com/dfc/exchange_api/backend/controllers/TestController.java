package com.dfc.exchange_api.backend.controllers;

import com.dfc.exchange_api.backend.services.ExternalApiService;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URISyntaxException;
import java.util.Optional;

@RestController
public class TestController {

    @Autowired
    ExternalApiService externalApiService;

    @GetMapping("latest/{base}")
    public ResponseEntity<JsonObject> getLatestsExchange(@PathVariable(name = "base") String base,
                                                         @RequestParam(name = "symbols", required = false) Optional<String> symbols) throws URISyntaxException {

        return ResponseEntity.ok().body(externalApiService.getLatestExchanges(base, symbols));
    }
}
