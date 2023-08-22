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
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ConversionService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConversionService.class);
    private static final String INPUT_REGEX = "[\n\r]";
    private CurrencyRepository currencyRepository;
    private CurrencyService currencyService;
    private ExchangeService exchangeService;

    public ConversionService(CurrencyRepository currencyRepository, CurrencyService currencyService, ExchangeService exchangeService) {
        this.currencyRepository = currencyRepository;
        this.currencyService = currencyService;
        this.exchangeService = exchangeService;
    }

    /**
     * This method returns the conversion value of a specified amount from a Currency A to another Currency B.
     * To do so, one of 2 executions path can happen:
     *      1. We will fetch the exchange rate of A -> B from the exchangeRate cache,
     *          in case it's stored there. We then calculate the conversion based on this rate.
     *      2. If the exchanged rate of A -> B is not in the exchangeRate cache, we will fetch it from the external API, do the
     *          conversion calculation, and store the fetched rate in its cache.
     * @param fromCode - the code of Currency A
     * @param toCode - the code of Currency B
     * @param amount - the amount to be converted
     * @return the value of the conversion
     * @throws InvalidCurrencyException - if either of the currency codes passed as parameters by the user is not supported
     *      * by the API, throws this exception with the HTTP Status BAD REQUEST
     * @throws ExternalApiConnectionError - in case of an error in the connection to the External API
     */
    public Double getConversionForSpecificCurrency(String fromCode, String toCode, Double amount) throws InvalidCurrencyException, ExternalApiConnectionError {
        // Verifying if the passed currencies are supported by the service
        if (!this.checkIfCurrencyExists(fromCode) || !this.checkIfCurrencyExists(toCode)) {
            LOGGER.info("One of the passed currencies is not supported by the service!");
            throw new InvalidCurrencyException("Invalid currency code(s) provided!");
        }

        // Checking if exchange rate is in the Cache
        Double exchangeRate = exchangeService.getExchangeRateFromCache(fromCode, toCode);

        if(exchangeRate == null){
            // Not in cache - needs to be fetched from the External API
            LOGGER.info("The exchange rate for {} is not in the cache", toCode.replaceAll(INPUT_REGEX, "_"));

            exchangeRate = exchangeService.getExchangeRatesFromExternalAPI(fromCode, toCode).get(toCode);
        }else{
            LOGGER.info("The exchange rate for {} is fetched from the cache", toCode.replaceAll(INPUT_REGEX, "_"));
        }

        // Calculating the conversion rates
        LOGGER.info("Finalizing processing the call to /convert/{from} endpoint with parameters: from - {}; to - {}", fromCode.replaceAll(INPUT_REGEX, "_"), toCode.replaceAll(INPUT_REGEX,"_"));
        return exchangeRate*amount;
    }


    /**
     * This method returns the conversion value of a specified amount from a Currency A to a list of Currencies B desired by the user.
     * To do so, one of 2 executions path can happen for each of the currencies in list B:
     *      1. If it's not in the conversionValue cache, we will fetch the exchange rate of A -> B from the exchangeRate cache,
     *          in case it's stored there. We then calculate the conversion based on this rate.
     *      2. If the exchanged rate of A -> B is not in the exchangeRate cache, we will fetch it from the external API, do the
     *          conversion calculation, and store the fetched rate in its cache.
     * @param fromCode - the fromCode of Currency A
     * @param toCurrencies - the list of supplied currencies to convert to
     * @param amount - the desired amount to be converted
     * @return a Map<String, Double> containing the conversion value for each supported currency (with their code being
     * the key of the map)
     * @throws InvalidCurrencyException - thrown when the user has passed an invalid code, with an HTTP Status BAD REQUEST
     * @throws ExternalApiConnectionError - in case of an error in the connection to the External API
     */
    public Map<String, Double> getConversionForVariousCurrencies(String fromCode, String toCurrencies, Double amount) throws InvalidCurrencyException, ExternalApiConnectionError {
        // Verifying if the currencies are supported by the service
        List<String> currencyToConvertCodes = new ArrayList<>(Arrays.asList(toCurrencies.split(",")));
        currencyToConvertCodes.add(fromCode);  // TO make verification easier

        currencyToConvertCodes.forEach(x -> {
                if (!this.checkIfCurrencyExists(x)) {
                    LOGGER.info("The passed currency {} is not supported by the service!", x);
                    throw new InvalidCurrencyException("Invalid currency code " + x + " provided!");
                }
            }
        );

        currencyToConvertCodes.remove(fromCode);  // No need to convert to own currency

        // The currencies are verified
        Map<String, Double> conversionValue = new HashMap<>();

        StringBuilder symbolsBuilder = new StringBuilder();                 // Will store symbols of currencies to be fetched from External API
        String symbols;                                                     // Will store the result of the StringBuilder

        // Checking if the passed Currencies exchange rate is in the cache or not; If not, contacting the External API
        currencyToConvertCodes.forEach(supportedCurrency -> {
            Double exchangeRate = exchangeService.getExchangeRateFromCache(fromCode, supportedCurrency);

            if (exchangeRate == null) {
                // Not in exchange cage - exchange rate needs to be retrieved from External API
                symbolsBuilder.append(supportedCurrency).append(",");
            }else{
                // ExchangeRate in ExchangeRate Cache
                conversionValue.put(supportedCurrency, exchangeRate*amount);
            }
        });

        // In case there is the need for it, contact the external API to retrieve new exchange rates
        if(!symbolsBuilder.isEmpty()){
            // There are currencies not present in the cache
            symbols = symbolsBuilder.toString();
        }else{
            LOGGER.info("No need to fetch exchange rates from external API");
            return conversionValue;
        }

        // Fetching from external API for any Currency not in Cache
        Map<String, Double> fetchedExchangeRates = exchangeService.getExchangeRatesFromExternalAPI(fromCode, symbols);

        fetchedExchangeRates.entrySet().forEach(entry -> entry.setValue(entry.getValue() * amount));
        conversionValue.putAll(fetchedExchangeRates);

        LOGGER.info("Finalizing processing the call to /exchange/{currency}/all endpoint with parameters: fromCode - {}", fromCode);
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
