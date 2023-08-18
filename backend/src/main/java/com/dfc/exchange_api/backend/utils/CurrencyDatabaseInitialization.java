package com.dfc.exchange_api.backend.utils;

import com.dfc.exchange_api.backend.services.CurrencyService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;


/**
 * This class defines a bean responsible for fetching the supported currencies from the external API, and then creating
 * the corresponding Currency domain entity instances on this API, storing them in the H2 in-memory database.
 * This process is done after the application initialization finishes, using CommandLineRunner, and the /symbols endpoint
 * of the external API.
 */
@Component
public class CurrencyDatabaseInitialization implements CommandLineRunner {
    private CurrencyService currencyService;

    public CurrencyDatabaseInitialization(CurrencyService currencyService) {
        this.currencyService = currencyService;
    }

    /**
     * Will call the currencyService's fetchSupportedCurrencies() method at application startup, to ensure that all of the
     * supported currencies from the external API are saved in the in-memory database.
     * @param args
     * @throws Exception
     */
    @Override
    public void run(String... args) throws Exception {
        currencyService.fetchSupportedCurrencies();
    }
}
