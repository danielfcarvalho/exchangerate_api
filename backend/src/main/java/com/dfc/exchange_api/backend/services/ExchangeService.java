package com.dfc.exchange_api.backend.services;

import com.dfc.exchange_api.backend.exceptions.ExternalApiConnectionError;
import com.dfc.exchange_api.backend.exceptions.InvalidCurrencyException;
import com.dfc.exchange_api.backend.models.Currency;
import com.dfc.exchange_api.backend.repositories.CurrencyRepository;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class ExchangeService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExchangeService.class);
    private static final String INPUT_REGEX = "[\n\r]";
    private ExternalApiService apiService;
    private CurrencyRepository currencyRepository;
    private CurrencyService currencyService;
    private CacheManager cacheManager;

    public ExchangeService(ExternalApiService apiService, CurrencyRepository currencyRepository, CurrencyService currencyService, CacheManager cacheManager) {
        this.apiService = apiService;
        this.currencyRepository = currencyRepository;
        this.currencyService = currencyService;
        this.cacheManager = cacheManager;
    }

    /**
     * This method returns the exchange rate from a currency A to a currency B. It implements the @Cacheable annotation,
     * meaning that it will first check the exchangeRate cache, which stores each individual exchange rate from a currency A to a
     * currency B, to see if the value is already stored. If it isn't, it contacts the external API at the /latest endpoint.
     * @param fromCode - the code for Currency A
     * @param toCode - the code for Currency B
     * @return the exchange rate
     * @throws InvalidCurrencyException - if either of the currency codes passed as parameters by the user is not supported
     * by the API, throws this exception with the HTTP Status BAD REQUEST
     * @throws ExternalApiConnectionError - in case of an error in the connection to the External API
     */
    @Cacheable(value = "exchangeRate", key = "#fromCode + '_' + #toCode")
    public Double getExchangeRateForSpecificCurrency(String fromCode, String toCode) throws InvalidCurrencyException, ExternalApiConnectionError {
        // Verifying if the passed currencies are supported by the service
        if (!this.checkIfCurrencyExists(fromCode) || !this.checkIfCurrencyExists(toCode)) {
            LOGGER.info("One of the passed currencies is not supported by the service!");
            throw new InvalidCurrencyException("Invalid currency code(s) provided!");
        }

        // Fetching from external API
        LOGGER.info("Fetching from external API the exchange rates from {} to {}", fromCode.replaceAll(INPUT_REGEX, "_"), toCode.replaceAll(INPUT_REGEX, "_"));
        JsonObject rates = apiService.getLatestExchanges(fromCode, Optional.of(toCode)).getAsJsonObject();

        LOGGER.info("Finalizing processing the call to /exchange/{from} endpoint with parameters: from - {}; to - {}", fromCode.replaceAll(INPUT_REGEX, "_"), toCode.replaceAll(INPUT_REGEX,"_"));
        return rates.get(toCode).getAsDouble();
    }


    /**
     * This method returns all the exchange rates for a given currency. To do so, it will first check, for each supported currency,
     * if the exchange rate is currently stored in the exchangeRate cache. If it is, that will be the rate returned for the specific currency.
     * In case there is a currency not stored in the cache, the external API will be contacted at the /latest endpoint,
     * using the currency as the base parameter, and the concatenated symbols of the uncached currencies as the symbols parameter,
     * via the ExternalApiService.
     * @param fromCode - the fromCode of the currency to be fetched
     * @return a Map<String, Double> containing the exchange rate for each supported currency (with their code being
     * the key of the map)
     * @throws InvalidCurrencyException - thrown when the user has passed an invalid code, with an HTTP Status BAD REQUEST
     * @throws ExternalApiConnectionError - in case of an error in the connection to the External API
     */
    public Map<String, Double> getExchangeRateForAll(String fromCode) throws InvalidCurrencyException, ExternalApiConnectionError {
        // Verifying if the currency is supported by the service
        if (!this.checkIfCurrencyExists(fromCode)) {
            LOGGER.info("The passed currency is not supported by the service!");
            throw new InvalidCurrencyException("Invalid currency code provided!");
        }

        Map<String, Double> exchangeRates = new HashMap<>();
        StringBuilder symbolsBuilder = new StringBuilder();                 // Will store symbols of currencies to be fetched from External API
        String symbols;                                                     // Will store the result of the StringBuilder
        Cache exchangeRateCache = cacheManager.getCache("exchangeRate");

        /* Looping the list of supported Currencies, to check for each one if they are stored in the Cache or if
        the external API needs to be contacted */
        currencyRepository.findAll().forEach(supportedCurrency -> {
            // Create the cache key for current Currency
            String supportedCurrencyCode = supportedCurrency.getCode();
            String cacheKey = fromCode + "_" + supportedCurrencyCode;

            // Try to fetch from the cache
            Cache.ValueWrapper cachedValue = exchangeRateCache.get(cacheKey);
            if(cachedValue == null){
                // Not in cache - needs to be fetched from the External API
                LOGGER.info("The exchange rate for {} is not in the cache", supportedCurrencyCode);
                symbolsBuilder.append(supportedCurrencyCode).append(",");
            }else{
                LOGGER.info("The exchange rate for {} is fetched from the cache", supportedCurrencyCode);
                exchangeRates.put(supportedCurrencyCode, (Double) cachedValue.get());
            }
        });

        // In case there is the need for it, contact the external API to retrieve new exchange rates
        if(!symbolsBuilder.isEmpty()){
            // There are currencies not present in the cache
            symbols = symbolsBuilder.toString();
        }else{
            LOGGER.info("No need to fetch exchange rates from external API");
            return exchangeRates;
        }

        // Fetching from external API
        LOGGER.info("Fetching from external API the required exchange rates from {}", fromCode.replaceAll(INPUT_REGEX, "_"));
        JsonObject rates = apiService.getLatestExchanges(fromCode, Optional.of(symbols)).getAsJsonObject();

        for(String key: rates.keySet()){
            Optional<Currency> exchangedCurrency = currencyRepository.findByCode(key);

            if(exchangedCurrency.isPresent()){
                String exchangedCurrencyCode = exchangedCurrency.get().getCode();
                Double exchangeValue = rates.get(key).getAsDouble();

                exchangeRates.put(exchangedCurrencyCode, exchangeValue);

                // Saving the new value in the cache
                if(exchangeRateCache != null){
                    exchangeRateCache.put(fromCode + "_" + exchangedCurrencyCode, exchangeValue);
                }
            }else{
                // A fetched currency isn't in the list of supported values. This means the list of supported symbols by the external
                // API has been updated since application startup, or that they have conversion rates for a symbol not present
                // in their /symbols endpoint. We should call the method to fetch currencies from the external API
                LOGGER.info("Fetched currency with fromCode {} was not on the repository! Contacting the fetchSupportedCurrencies() service", key);
                currencyService.fetchSupportedCurrencies();
            }
        }

        LOGGER.info("Finalizing processing the call to /exchange/{from}/all endpoint with parameters: fromCode - {}", fromCode);
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
