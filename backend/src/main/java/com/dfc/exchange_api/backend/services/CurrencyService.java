package com.dfc.exchange_api.backend.services;

import com.dfc.exchange_api.backend.exceptions.ExternalApiConnectionError;
import com.dfc.exchange_api.backend.models.Currency;
import com.dfc.exchange_api.backend.models.CurrencyDTO;
import com.dfc.exchange_api.backend.models.FetchedSymbolsDTO;
import com.dfc.exchange_api.backend.repositories.CurrencyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;

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
            FetchedSymbolsDTO fetchedCurrencies = externalApiService.getAvailableCurrencies();

            // Fetching any previously supported currencies that are no longer supported
            Set<String> currentlySupportedCurrencies = fetchedCurrencies.getSymbols().keySet();
            List<Currency> outdatedCurrencies = currencyRepository.findAll()
                    .stream()
                    .filter(currency -> !currentlySupportedCurrencies.contains(currency.getCode()))
                    .toList();

            // Deleting no longer supported currencies
            if(!outdatedCurrencies.isEmpty()){
                LOGGER.info("Detected outdated currencies. They will be removed from the repository.");
                currencyRepository.deleteAll(outdatedCurrencies);
            }

            // Checking if any of the fetched currencies is new; adding new currencies to the repository
            for (Map.Entry<String, CurrencyDTO> entry : fetchedCurrencies.getSymbols().entrySet()) {
                CurrencyDTO currentFetchedCurrency = entry.getValue();
                String description = currentFetchedCurrency.getDescription();
                String code = currentFetchedCurrency.getCode();

                if (currencyRepository.findByCode(code).isEmpty()) {
                    // If currency is not already in the repository
                    Currency currentCurrency = new Currency(description, code);
                    LOGGER.info("Fetched currency: {}", currentCurrency);
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

    /**
     * Method used to get all the currencies supported by this API, called by the REST endpoint associated with this
     * call.
     * @return the list of supported currencies
     */
    public List<Currency> getSupportedCurrencies(){
        return currencyRepository.findAll();
    }
}
