package com.dfc.exchange_api.backend.utils;

import com.dfc.exchange_api.backend.repositories.CurrencyRepository;
import com.dfc.exchange_api.backend.services.ExternalApiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(CurrencyDatabaseInitialization.class);

    private CurrencyRepository currencyRepository;
    private ExternalApiService externalApiService;

    public CurrencyDatabaseInitialization(CurrencyRepository currencyRepository, ExternalApiService externalApiService) {
        this.currencyRepository = currencyRepository;
        this.externalApiService = externalApiService;
    }

    @Override
    public void run(String... args) throws Exception {

    }
}
