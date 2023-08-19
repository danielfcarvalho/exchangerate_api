package com.dfc.exchange_api.backend.controllers;

import com.dfc.exchange_api.backend.exceptions.ExternalApiConnectionError;
import com.dfc.exchange_api.backend.exceptions.InvalidCurrencyException;
import com.dfc.exchange_api.backend.services.ExchangeService;
import com.dfc.exchange_api.backend.services.ExternalApiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/exchange")
public class ExchangeController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExternalApiService.class);
    private ExchangeService exchangeService;

    public ExchangeController(ExchangeService exchangeService) {
        this.exchangeService = exchangeService;
    }

    /**
     * This endpoint is used to fetch the exchange rate from a currency A to a Currency B.
     * @param from - the code of currency A
     * @param to - the code of currency B
     * @return A response entity containing a Map with the value of the conversion rate for each supported currency, and
     * with HTTP status code OK.
     * @throws InvalidCurrencyException - In case either the currency A or B are not supported or have an invalid code,
     * this exception is thrown with HTTP status BAD REQUEST.
     * @throws ExternalApiConnectionError - In case communication with the External API fails, this exception is thrown
     * with Http Status BAD GATEWAY.
     */
    @GetMapping("/{currency}")
    public ResponseEntity<Map<String, Double>> getExchangeRateForSpecificCurrency(
            @PathVariable(name = "currency") String from,
            @RequestParam(name = "to") String to)
            throws InvalidCurrencyException, ExternalApiConnectionError {
        LOGGER.info("Received a request on the /exchange/{currency} endpoint with parameters: from - {}; to - {}", from, to);

        return ResponseEntity.ok().body(exchangeService.getExchangeRateForSpecificCurrency(from, to));
    }

    @GetMapping("/{currency}/all")
    public ResponseEntity<Map<String, Double>> getExchangeRateForAll(
            @PathVariable(name = "currency") String code)
            throws InvalidCurrencyException, ExternalApiConnectionError{
        LOGGER.info("Received a request on the /exchange/{currency}/all endpoint with parameters: code - {}", code);

        return ResponseEntity.ok().body(exchangeService.getExchangeRateForAll(code));
    }
}
