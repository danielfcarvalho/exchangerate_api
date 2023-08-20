package com.dfc.exchange_api.backend.services;

import com.dfc.exchange_api.backend.exceptions.ExternalApiConnectionError;
import com.dfc.exchange_api.backend.exceptions.InvalidCurrencyException;
import com.dfc.exchange_api.backend.models.Currency;
import com.dfc.exchange_api.backend.repositories.CurrencyRepository;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ConversionService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConversionService.class);
    private static final String INPUT_REGEX = "[\n\r]";
    private ExternalApiService apiService;
    private CurrencyRepository currencyRepository;
    private CurrencyService currencyService;

    public ConversionService(ExternalApiService apiService, CurrencyRepository currencyRepository, CurrencyService currencyService) {
        this.apiService = apiService;
        this.currencyRepository = currencyRepository;
        this.currencyService = currencyService;
    }

    public Map<String, Double> getConversionForSpecificCurrency(String fromCode, String toCode, Double amount) throws InvalidCurrencyException, ExternalApiConnectionError {
        // Verifying if the passed currencies are supported by the service
        if (!this.checkIfCurrencyExists(fromCode) || !this.checkIfCurrencyExists(toCode)) {
            LOGGER.info("One of the passed currencies is not supported by the service!");
            throw new InvalidCurrencyException("Invalid currency code(s) provided!");
        }

        Map<String, Double> conversionValue = new HashMap<>();

        //TODO: Check if exchange rate is in the Cache

        // Fetching from external API
        LOGGER.info("Fetching from external API the exchange rates from {} to {}", fromCode.replaceAll(INPUT_REGEX, "_"), toCode.replaceAll(INPUT_REGEX, "_"));
        JsonObject rates = apiService.getLatestExchanges(fromCode, Optional.of(toCode)).getAsJsonObject();

        //TODO: Add exchange Rates to Cache

        // Calculating the conversion rates
        conversionValue.put(toCode, rates.get(toCode).getAsDouble() * amount);
        LOGGER.info("Finalizing processing the call to /convert/{from} endpoint with parameters: from - {}; to - {}", fromCode.replaceAll(INPUT_REGEX, "_"), toCode.replaceAll(INPUT_REGEX,"_"));
        return conversionValue;
    }


    public Map<String, Double> getConversionForVariousCurrencies(String code, String toCurrencies, Double amount) throws InvalidCurrencyException, ExternalApiConnectionError {
        // Verifying if the currencies are supported by the service
        List<String> currencyCodes = new ArrayList<>(Arrays.asList(toCurrencies.split(",")));
        currencyCodes.add(code);

        currencyCodes.forEach(x -> {
                if (!this.checkIfCurrencyExists(x)) {
                    LOGGER.info("The passed currency {} is not supported by the service!", x);
                    throw new InvalidCurrencyException("Invalid currency code " + x + " provided!");
                }
            }
        );

        Map<String, Double> conversionValue = new HashMap<>();

        //TODO: Check if exchange rates are in the Cache


        // Fetching from external API
        //TODO: Do only for coins who are not in the Cache
        LOGGER.info("Fetching from external API the exchange rates from {} to {}", code.replaceAll(INPUT_REGEX, "_"), toCurrencies.replaceAll(INPUT_REGEX, "_"));
        JsonObject rates = apiService.getLatestExchanges(code, Optional.of(toCurrencies)).getAsJsonObject();

        for(String key: rates.keySet()){
            //TODO: Add to cache
            Optional<Currency> exchangedCurrency = currencyRepository.findByCode(key);

            if(exchangedCurrency.isPresent()){
                conversionValue.put(exchangedCurrency.get().getCode(), rates.get(key).getAsDouble() * amount);
            }else{
                // A fetched currency isn't in the list of supported values. This means the list of supported symbols by the external
                // API has been updated since application startup, or that they have conversion rates for a symbol not present
                // in their /symbols endpoint. We should call the method to fetch currencies from the external API
                LOGGER.info("Fetched currency with code {} was not on the repository! Contacting the fetchSupportedCurrencies() service", key);
                currencyService.fetchSupportedCurrencies();
            }
        }

        LOGGER.info("Finalizing processing the call to /exchange/{currency}/all endpoint with parameters: code - {}", code);
        return conversionValue;
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
