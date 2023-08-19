package com.dfc.exchange_api.backend.services;

import com.dfc.exchange_api.backend.exceptions.ExternalApiConnectionError;
import com.dfc.exchange_api.backend.models.Currency;
import com.dfc.exchange_api.backend.repositories.CurrencyRepository;
import com.dfc.exchange_api.backend.utils.CurrencyDatabaseInitialization;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class CurrencyService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CurrencyService.class);

    private CurrencyRepository currencyRepository;
    private ExternalApiService externalApiService;

    public CurrencyService(CurrencyRepository currencyRepository, ExternalApiService externalApiService) {
        this.currencyRepository = currencyRepository;
        this.externalApiService = externalApiService;
    }

    /**
     * This method will fetch the supported currencies form the External API. To do so, it will contact the /symbols
     * endpoint. Then, it will parse the obtained JSON Object, corresponding to an array in which each entry is a currency,
     * and will create an instance of the Currency object. In the end, it will save all Currency objects in the currency repository,
     * thus storing it in the in-memory H2 database.
     * This method is scheduled to run every hour, to check whether the list of supported symbols by the External API has been
     * updated or not. Thus, before adding a currency to the repository, we check whether or not that currency is already stored
     * (to avoid the creation of duplicates).
     */
    @Scheduled(initialDelay = 3600000, fixedRate = 3600000)
    public void fetchSupportedCurrencies() {
        List<Currency> supportedCurrencies = new ArrayList<>();

        // Fetching supported currencies by the external API
        LOGGER.info("Fetching list of supported currencies by the external API....");

        try {
            JsonObject currenciesJSON = externalApiService.getAvailableCurrencies().getAsJsonObject();

            for (Map.Entry<String, JsonElement> entry : currenciesJSON.entrySet()) {
                JsonObject currentObject = entry.getValue().getAsJsonObject(); // {"description": ..., "code": ....}
                String description = currentObject.getAsJsonPrimitive("description").getAsString();
                String code = currentObject.getAsJsonPrimitive("code").getAsString();

                if (currencyRepository.findByCode(code).isEmpty()) {
                    // If currency is not already in the repository
                    Currency currentCurrency = new Currency(description, code);
                    LOGGER.info("Fetched currency: {}", currentCurrency.toString());
                    supportedCurrencies.add(currentCurrency);
                }
            }

            if (!supportedCurrencies.isEmpty()) {
                LOGGER.info("Adding new supported currencies to the repository.");
                currencyRepository.saveAll(supportedCurrencies);
            }
        }
        catch(ExternalApiConnectionError e){
                LOGGER.info("Could not connext to External API");
            }
    }
}
