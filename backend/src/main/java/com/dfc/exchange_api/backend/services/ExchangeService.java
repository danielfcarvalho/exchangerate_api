package com.dfc.exchange_api.backend.services;

import com.dfc.exchange_api.backend.models.Currency;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Service
public class ExchangeService {
    private ExternalApiService apiService;

    public ExchangeService(ExternalApiService apiService) {
        this.apiService = apiService;
    }

    public Map<Currency, Double> getExchangeRateForSpecificCurrency(String fromCode, String toCode) {
        return null;
    }


    public Map<Currency, Double> getExchangeRateForAll(String code) {
        return null;
    }
}
