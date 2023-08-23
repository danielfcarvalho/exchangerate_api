package com.dfc.exchange_api.backend.controllers;

import com.dfc.exchange_api.backend.models.Currency;
import com.dfc.exchange_api.backend.services.CurrencyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@Tag(name = "4. Currency Controller", description = "Endpoint to retrieve supported currencies, based on the supported currencies of the External API")
@RestController
@RequestMapping("/api/v1/currency")
public class CurrencyController {
    private static final Logger LOGGER = LoggerFactory.getLogger(CurrencyController.class);
    private CurrencyService currencyService;

    public CurrencyController(CurrencyService currencyService) {
        this.currencyService = currencyService;
    }

    /**
     * This endpoint returns all the currencies supported by this API. The list of supported currencies is fetched from the list
     * of supported symbols by the external API at application startup, and is updated via a scheduled job, that runs every hour.
     * @return the list of supported currencies
     */
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Valid currency code",
                    content = @Content),})
    @Operation(summary = "Get the list of currencies supported by the API")
    @GetMapping()
    public ResponseEntity<List<Currency>> getSupportedCurrencies() {
        LOGGER.info("Received a request on the GET /currency endpoint");

        return ResponseEntity.ok().body(currencyService.getSupportedCurrencies());
    }
}
