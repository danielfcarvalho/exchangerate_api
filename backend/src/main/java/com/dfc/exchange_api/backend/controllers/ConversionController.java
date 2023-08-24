package com.dfc.exchange_api.backend.controllers;

import com.dfc.exchange_api.backend.exceptions.ExternalApiConnectionError;
import com.dfc.exchange_api.backend.exceptions.InvalidCurrencyException;
import com.dfc.exchange_api.backend.services.ConversionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.PositiveOrZero;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "2. Conversion Controller", description = "Endpoint to convert amounts from a supplied currency")
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
     * This endpoint fetches the conversion of a specified amount from currency A to a list of currencies. This list can
     * be composed of either 1 specific currency B, or more currencies, with their codes being separated by commas.
     * @param from - the code of currency A
     * @param to - the codes of the specified list of currencies for the conversion, separated by commas
     * @param amount - the amount to be converted
     * @return a map containing as key the codes of the currencies to be converted to, and as value the value of the conversion
     * @throws InvalidCurrencyException - In case either the currency A, or the specified currency B are not supported or have an invalid code,
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
    @Operation(summary = "Get the conversion of a specified amount from currency A o a list of specified currencies, separated by commas")
    @GetMapping
    public Map<String, Double> getConversionFromCurrency(
            @Parameter(description = "The code of currency A", required = true) @RequestParam(name = "from") String from,
            @Parameter(description = "The codes of the specified currencies, separated by commas", required = true) @RequestParam(name = "to") String to,
            @Parameter(description = "The amount to be converted", required = true) @RequestParam(name = "amount") @PositiveOrZero(message = "Amount must be non-negative!") Double amount)
            throws InvalidCurrencyException, ExternalApiConnectionError {
        LOGGER.info("Received a request on the GET /convert endpoint");

        return conversionService.getConversionFromCurrency(from.toUpperCase(), to.toUpperCase(), amount);
    }
}
