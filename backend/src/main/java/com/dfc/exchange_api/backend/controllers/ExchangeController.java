package com.dfc.exchange_api.backend.controllers;

import com.dfc.exchange_api.backend.exceptions.ExternalApiConnectionError;
import com.dfc.exchange_api.backend.exceptions.InvalidCurrencyException;
import com.dfc.exchange_api.backend.services.ExchangeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "1. Exchange Controller", description = "Endpoint to determine the exchange rate from a given currency")
@RestController
@RequestMapping("/api/v1/exchange")
public class ExchangeController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExchangeController.class);
    private ExchangeService exchangeService;

    public ExchangeController(ExchangeService exchangeService) {
        this.exchangeService = exchangeService;
    }

    /**
     * This endpoint is used to fetch the exchange rates from a currency A, passed as a required from parameter.
     * Users can pass an optional "to" parameter - if this is passed, the endpoint will return the exchange rate from currency
     * A to a specific currency B; otherwise, it will return all the exchange rates from A to all supported currencies.
     * @param from - the code of currency A
     * @param to - the optional code of currency B
     * @return A map with the value of the exchange rate from A to the specified currencies, with their code being the key,
     * and the value being the exchange rate.
     * @throws InvalidCurrencyException - In case either the currency A, or the specified currency B are not supported or have an invalid code,
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
    @Operation(summary = "Get the exchange rates from currency A to either a currency B, if \"to\" is present, or all supported currencies, if \"to\" is absent")
    @GetMapping
    public Map<String, Double> getExchangeRateFromCurrency(
            @Parameter(description = "The code of currency A", required = true) @RequestParam(name = "from") String from,
            @Parameter(description = "The code of currency B") @RequestParam(name = "to", required = false) String to)
            throws InvalidCurrencyException, ExternalApiConnectionError {
        LOGGER.info("Received a request on the GET /exchange endpoint");

        if(to != null){
            // Exchange Rate for a Specific Currency
            LOGGER.info("Request for a specific exchange rate");

            return Map.of(to, exchangeService.getExchangeRateForSpecificCurrency(from.toUpperCase(), to.toUpperCase()));
        }else{
            // Exchange Rate for all Currencies
            LOGGER.info("Request for all exchange rates");

            return exchangeService.getExchangeRateForAll(from.toUpperCase());
        }
    }
}
