package com.dfc.exchange_api.backend.controllers;

import com.dfc.exchange_api.backend.services.ExchangeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("exchanges")
public class ExchangeController {
    private ExchangeService exchangeService;

    public ExchangeController(ExchangeService exchangeService) {
        this.exchangeService = exchangeService;
    }

    @GetMapping("/{currency}")
    public ResponseEntity<Map<String, String>> getExchangeRateForSpecificCurrency(
            @RequestParam(name = "to") String to) {
        return null;
    }

    @GetMapping("/{currency}/all")
    public ResponseEntity<Map<String, String>> getExchangeRateForAll() {
        return null;
    }
}
