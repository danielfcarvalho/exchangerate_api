package com.dfc.exchange_api.backend.utils;

import com.dfc.exchange_api.backend.models.Currency;
import com.dfc.exchange_api.backend.repositories.CurrencyRepository;
import com.dfc.exchange_api.backend.services.ExternalApiService;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class defines a bean responsible for fetching the supported currencies from the external API, and then creating
 * the corresponding Currency domain entity instances on this API, storing them in the H2 in-memory database.
 * This process is done after the application initialization finishes, using CommandLineRunner, and the /symbols endpoint
 * of the external API.
 */
@Component
public class CurrencyDatabaseInitialization implements CommandLineRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(CurrencyDatabaseInitialization.class);

    private CurrencyRepository currencyRepository;
    private ExternalApiService externalApiService;

    public CurrencyDatabaseInitialization(CurrencyRepository currencyRepository, ExternalApiService externalApiService) {
        this.currencyRepository = currencyRepository;
        this.externalApiService = externalApiService;
    }

    /**
     * This method will fetch the supported currencies form the External API. To do so, it will contacte the /symbols
     * endpoint. Then, it will aprse the obtained JSON Object, corresponding to an array in which each entry is a currency,
     * and will create an instance of the Currency object. In the end, it will save all Currency objects in the currency repository,
     * thus storing it in the in-memory H2 database.
     * @param args
     * @throws Exception
     */
    @Override
    public void run(String... args) throws Exception {
        List<Currency> supportedCurrencies = new ArrayList<>();

        // Fetching supported currencies by the external API
        LOGGER.info("Fetching list of supported currencies by the external API....");
        JsonObject currenciesJSON = externalApiService.getAvailableCurrencies().getAsJsonObject();

        for (Map.Entry<String, JsonElement> entry: currenciesJSON.entrySet()){
            JsonObject currentObject = entry.getValue().getAsJsonObject(); // {"description": ..., "code": ....}
            Currency currentCurrency = new Currency(currentObject.getAsJsonPrimitive("description").getAsString(),
                    currentObject.getAsJsonPrimitive("code").getAsString());

            LOGGER.info("Fetched currency: " + currentCurrency.toString());
            supportedCurrencies.add(currentCurrency);
        }

        // Adding the currencies to the in-memory database
        LOGGER.info("Finalized fetching all supported currencies.");
        currencyRepository.saveAll(supportedCurrencies);
    }
}
