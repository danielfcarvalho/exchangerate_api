package com.dfc.exchange_api.backend.controllers;

import com.dfc.exchange_api.backend.models.Currency;
import com.dfc.exchange_api.backend.services.ExchangeService;
import com.dfc.exchange_api.backend.services.ExternalApiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("exchange")
public class ExchangeController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExternalApiService.class);
    private ExchangeService exchangeService;

    public ExchangeController(ExchangeService exchangeService) {
        this.exchangeService = exchangeService;
    }

    @GetMapping("/{currency}")
    public ResponseEntity<Map<String, Double>> getExchangeRateForSpecificCurrency(
            @RequestParam(name = "to") String to) {
        return null;
    }

    @GetMapping("/{currency}/all")
    public ResponseEntity<Map<String, Double>> getExchangeRateForAll(@PathVariable(name = "currency") String code) {
        return null;
    }
}
