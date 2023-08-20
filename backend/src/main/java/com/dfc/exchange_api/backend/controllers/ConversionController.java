package com.dfc.exchange_api.backend.controllers;

import com.dfc.exchange_api.backend.exceptions.ExternalApiConnectionError;
import com.dfc.exchange_api.backend.exceptions.InvalidCurrencyException;
import com.dfc.exchange_api.backend.services.ConversionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.PositiveOrZero;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Conversion Controller", description = "Endpoints to convert amounts from a supplied currency")
@RestController
@Validated
@RequestMapping("/api/v1/convert")
public class ConversionController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConversionController.class);
    public ConversionService conversionService;

    public ConversionController(ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Valid currency code",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid currency code supplied",
                    content = @Content),
            @ApiResponse(responseCode = "402", description = "Error connecting to external API",
                    content = @Content),})
    @Operation(summary = "Get the exchange rates from a currency to all other supported currencies")
    @GetMapping("/{from}")
    public ResponseEntity<Map<String, Double>> getConversionForSpecificCurrency(
            @PathVariable(name = "from") String from,
            @RequestParam(name = "to") String to,
            @RequestParam(name = "amount") @PositiveOrZero(message = "Amount must be non-negative!") Double amount)
            throws InvalidCurrencyException, ExternalApiConnectionError {
        LOGGER.info("Received a request on the /convert/{currency} endpoint");

        return null;
    }

    @GetMapping("/{from}/all")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Valid currency code",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid currency code supplied",
                    content = @Content),
            @ApiResponse(responseCode = "402", description = "Error connecting to external API",
                    content = @Content),})
    @Operation(summary = "Get the exchange rates from a currency A to a currency B")
    public ResponseEntity<Map<String, Double>> getConversionForAll(
            @PathVariable(name = "from") String code,
            @RequestParam(name = "amount") @PositiveOrZero(message = "Amount must be non-negative!")  Double amount)
            throws InvalidCurrencyException, ExternalApiConnectionError{
        LOGGER.info("Received a request on the /convert/{currency}/all endpoint");

        return null;
    }
}
