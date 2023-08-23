package com.dfc.exchange_api.backend.controllers;

import com.dfc.exchange_api.backend.exceptions.ExternalApiConnectionError;
import com.dfc.exchange_api.backend.exceptions.InvalidCurrencyException;
import com.dfc.exchange_api.backend.services.ExchangeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "1. Exchange Controller", description = "Endpoints to determine the exchange rate from a given currency")
@RestController
@RequestMapping("/api/v1/exchange")
public class ExchangeController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExchangeController.class);
    private ExchangeService exchangeService;

    public ExchangeController(ExchangeService exchangeService) {
        this.exchangeService = exchangeService;
    }

    /**
     * This endpoint is used to fetch the exchange rate from a currency A to a Currency B.
     * @param from - the code of currency A
     * @param to - the code of currency B
     * @return A response entity containing a Map with the value of the conversion rate for the specified currency, and
     * with HTTP status code OK.
     * @throws InvalidCurrencyException - In case either the currency A or B are not supported or have an invalid code,
     * this exception is thrown with HTTP status BAD REQUEST.
     * @throws ExternalApiConnectionError - In case communication with the External API fails, this exception is thrown
     * with Http Status BAD GATEWAY.
     */
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Valid currency code",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid currency code supplied",
                    content = @Content),
            @ApiResponse(responseCode = "402", description = "Error connecting to external API",
                    content = @Content),})
    @Operation(summary = "Get the exchange rates from currency A to a currency B")
    @GetMapping("/{from}")
    public ResponseEntity<Map<String, Double>> getExchangeRateForSpecificCurrency(
            @PathVariable(name = "from") String from,
            @RequestParam(name = "to") String to)
            throws InvalidCurrencyException, ExternalApiConnectionError {
        LOGGER.info("Received a request on the GET /exchange/{currency} endpoint");

        return ResponseEntity.ok().body(Map.of(to, exchangeService.getExchangeRateForSpecificCurrency(from.toUpperCase(), to.toUpperCase())));
    }

    /**
     * This endpoint is used to fetch the exchange rate from a currency A to all the supported currencies.
     * @param code - the code of currency A
     * @return A response entity containing a Map with the value of the conversion rate for each supported currency, and
     * with HTTP status code OK.
     * @throws InvalidCurrencyException - In case currency A is not supported or has an invalid code,
     * this exception is thrown with HTTP status BAD REQUEST.
     * @throws ExternalApiConnectionError - In case communication with the External API fails, this exception is thrown
     * with Http Status BAD GATEWAY.
     */
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Valid currency code",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid currency code supplied",
                    content = @Content),
            @ApiResponse(responseCode = "402", description = "Error connecting to external API",
                    content = @Content),})
    @Operation(summary = "Get the exchange rates from a currency to all other supported currencies")
    @GetMapping("/{from}/all")
    public ResponseEntity<Map<String, Double>> getExchangeRateForAll(
            @PathVariable(name = "from") String code)
            throws InvalidCurrencyException, ExternalApiConnectionError{
        LOGGER.info("Received a request on the GET /exchange/{currency}/all endpoint");

        return ResponseEntity.ok().body(exchangeService.getExchangeRateForAll(code.toUpperCase()));
    }
}
