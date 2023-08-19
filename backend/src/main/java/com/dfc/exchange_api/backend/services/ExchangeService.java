package com.dfc.exchange_api.backend.services;

import com.dfc.exchange_api.backend.exceptions.ExternalApiConnectionError;
import com.dfc.exchange_api.backend.exceptions.InvalidCurrencyException;
import com.dfc.exchange_api.backend.models.Currency;
import com.dfc.exchange_api.backend.repositories.CurrencyRepository;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class ExchangeService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExchangeService.class);
    private ExternalApiService apiService;
    private CurrencyRepository currencyRepository;
    private CurrencyService currencyService;

    public ExchangeService(ExternalApiService apiService, CurrencyRepository currencyRepository, CurrencyService currencyService) {
        this.apiService = apiService;
        this.currencyRepository = currencyRepository;
        this.currencyService = currencyService;
    }

    /**
     * This method returns the exchange rate from a currency A to a currency B. To do so, it contacts the external API at
     * the /latest endpoint.
     * @param fromCode - the code for Currency A
     * @param toCode - the code for Currency B
     * @return a Map<String, Double> containing the exchange rate
     * @throws InvalidCurrencyException - if either of the currency codes passed as parameters by the user is not supported
     * by the API, throws this exception with the HTTP Status BAD REQUEST
     */
    public Map<String, Double> getExchangeRateForSpecificCurrency(String fromCode, String toCode) throws InvalidCurrencyException, ExternalApiConnectionError {
        // Verifying if the passed currencies are supported by the service
        if (!this.checkIfCurrencyExists(fromCode) || !this.checkIfCurrencyExists(toCode)) {
            LOGGER.info("One of the passed currencies is not supported by the service!");
            throw new InvalidCurrencyException("Invalid currency code(s) provided!");
        }

        Map<String, Double> exchangeRates = new HashMap<>();

        // Fetching from external API
        LOGGER.info("Fetching from external API the exchange rates from {} to {}", fromCode.replaceAll("[\n\r]", "_"), toCode.replaceAll("[\n\r]", "_"));
        JsonObject rates = apiService.getLatestExchanges(fromCode, Optional.of(toCode)).getAsJsonObject();

        exchangeRates.put(toCode, rates.get(toCode).getAsDouble());
        LOGGER.info("Finalizing processing the call to /exchange/{currency} endpoint with parameters: from - {}; to - {}", fromCode.replaceAll("[\n\r]", "_"), toCode.replaceAll("[\n\r]", "_"));
        return exchangeRates;
    }


    /**
     * This method returns all the exchange rates for a given currency. To do so, it contacts the external API at the
     * /latest endpoint, using the currency as the base parameter, via the ExternalApiService.
     * @param code - the code of the currency to be fetched
     * @return a Map<String, Double> containing the exchange rate for each supported currency (with their code being
     * the key of the map)
     * @throws InvalidCurrencyException - thrown when the user has passed an invalid code, with an HTTP Status BAD REQUEST
     */
    public Map<String, Double> getExchangeRateForAll(String code) throws InvalidCurrencyException, ExternalApiConnectionError {
        // Verifying if the currency is supported by the service
        if (!this.checkIfCurrencyExists(code)) {
            LOGGER.info("The passed currency is not supported by the service!");
            throw new InvalidCurrencyException("Invalid currency code provided!");
        }

        Map<String, Double> exchangeRates = new HashMap<>();

        // Fetching from external API
        LOGGER.info("Fetching from external API the exchange rates from {}", code.replaceAll("[\n\r]", "_"));
        JsonObject rates = apiService.getLatestExchanges(code, Optional.empty()).getAsJsonObject();

        for(String key: rates.keySet()){
            Optional<Currency> exchangedCurrency = currencyRepository.findByCode(key);

            if(exchangedCurrency.isPresent()){
                exchangeRates.put(exchangedCurrency.get().getCode(), rates.get(key).getAsDouble());
            }else{
                // A fetched currency isn't in the list of supported values. This means the list of supported symbols by the external
                // API has been updated since application startup, or that they have conversion rates for a symbol not present
                // in their /symbols endpoint. We should call the method to fetch currencies from the external API
                LOGGER.info("Fetched currency with code {} was not on the repository! Contacting the fetchSupportedCurrencies() service", key);
                currencyService.fetchSupportedCurrencies();
            }
        }

        LOGGER.info("Finalizing processing the call to /exchange/{currency}/all endpoint with parameters: code - {}", code);
        return exchangeRates;
    }

    /**
     * Auxilliary method that checks if a code passed as a parameter by the user in an API request belongs to a supported
     * currency or not.
     * @param code - the code of the Currency to be checked
     * @return a boolean representing whether the code is supported or not by the API
     */
    private boolean checkIfCurrencyExists(String code) {
        return currencyRepository.existsByCode(code);
    }
}
