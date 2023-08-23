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

@Tag(name = "2. Conversion Controller", description = "Endpoints to convert amounts from a supplied currency")
@RestController
@Validated
@RequestMapping("/api/v1/convert")
public class ConversionController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConversionController.class);
    private ConversionService conversionService;

    public ConversionController(ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    /**
     * This endpoint is used to fetch the conversion from a currency A to a Currency B.
     * @param from - the code of currency A
     * @param to - the code of currency B
     * @param amount - the amount to convert
     * @return A response entity containing a Map with the value of the conversion for the specified currency, and
     * with HTTP status code OK.
     * @throws InvalidCurrencyException - In case either the currency A or B are not supported or have an invalid code,
     * this exception is thrown with HTTP status BAD REQUEST.
     * @throws ExternalApiConnectionError - In case communication with the External API fails, this exception is thrown
     * with Http Status BAD GATEWAY.
     */
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Valid currency codes and amount to convert",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid currency codes or amount supplied",
                    content = @Content),
            @ApiResponse(responseCode = "402", description = "Error connecting to external API",
                    content = @Content),})
    @Operation(summary = "Get the conversion of a specified amount from currency A to a currency B")
    @GetMapping("/{from}")
    public ResponseEntity<Map<String, Double>> getConversionForSpecificCurrency(
            @PathVariable(name = "from") String from,
            @RequestParam(name = "to") String to,
            @RequestParam(name = "amount") @PositiveOrZero(message = "Amount must be non-negative!") Double amount)
            throws InvalidCurrencyException, ExternalApiConnectionError {
        LOGGER.info("Received a request on the GET /convert/{from} endpoint");

        return ResponseEntity.ok().body(Map.of(to, conversionService.getConversionForSpecificCurrency(from, to, amount)));
    }

    /**
     * This endpoint is used to fetch the conversion of a specified amount from a currency A to supplied list of
     * currencies.
     * @param from - the code of currency A
     * @param to - the codes of the supplied list of currencies to convert to
     * @param amount - the amount to be converted
     * @return A response entity containing a Map with the value of the conversion rate for each supplied currency, and
     * with HTTP status code OK.
     * @throws InvalidCurrencyException - In case currency A or one of the supplied currencies is not supported or has an invalid code,
     * this exception is thrown with HTTP status BAD REQUEST.
     * @throws ExternalApiConnectionError - In case communication with the External API fails, this exception is thrown
     * with Http Status BAD GATEWAY.
     */
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Valid currency codes and amount to convert",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid currency codes or amount supplied",
                    content = @Content),
            @ApiResponse(responseCode = "402", description = "Error connecting to external API",
                    content = @Content),})
    @Operation(summary = "Get the conversion of a specified amount from Currency A to a list of supplied currencies")
    @GetMapping("/{from}/various")
    public ResponseEntity<Map<String, Double>> getConversionForAll(
            @PathVariable(name = "from") String from,
            @RequestParam(name = "to") String to,
            @RequestParam(name = "amount") @PositiveOrZero(message = "Amount must be non-negative!")  Double amount)
            throws InvalidCurrencyException, ExternalApiConnectionError{
        LOGGER.info("Received a request on the GET /convert/{from}/various endpoint");

        return ResponseEntity.ok().body(conversionService.getConversionForVariousCurrencies(from, to, amount));
    }
}
