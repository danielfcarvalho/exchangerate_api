package com.dfc.exchange_api.backend.services;

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
    private static final Logger LOGGER = LoggerFactory.getLogger(ExternalApiService.class);
    private ExternalApiService apiService;
    private CurrencyRepository currencyRepository;
    private CurrencyService currencyService;

    public ExchangeService(ExternalApiService apiService, CurrencyRepository currencyRepository, CurrencyService currencyService) {
        this.apiService = apiService;
        this.currencyRepository = currencyRepository;
        this.currencyService = currencyService;
    }

    public Map<String, Double> getExchangeRateForSpecificCurrency(String fromCode, String toCode) {
        return null;
    }


    public Map<String, Double> getExchangeRateForAll(String code) throws InvalidCurrencyException {
        // TODO: Add Logger and Javadoc
        // Verifying if the currency is supported by the service
        Currency coin = currencyRepository.findByCode(code).orElseThrow(() -> new InvalidCurrencyException(code + " is not a valid currency code!"));

        Map<String, Double> exchangeRates = new HashMap<>();

        // Fetching from external API
        JsonObject rates = apiService.getLatestExchanges(code, Optional.empty()).getAsJsonObject();

        for(String key: rates.keySet()){
            Optional<Currency> exchangedCurrency = currencyRepository.findByCode(key);

            if(exchangedCurrency.isPresent()){
                exchangeRates.put(exchangedCurrency.get().getCode(), rates.get(key).getAsDouble());
            }else{
                // A fetched currency isn't in the list of supported values. This means the list of supported symbols by the external
                // API has been updated since application startup, or that they have conversion rates for a symbol not present
                // in their /symbols endpoint. We should call the method to fetch currencies from the external API
                currencyService.fetchSupportedCurrencies();
            }
        }

        return exchangeRates;
    }
}
