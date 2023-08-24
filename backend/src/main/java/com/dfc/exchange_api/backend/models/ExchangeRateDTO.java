package com.dfc.exchange_api.backend.models;

import lombok.*;

import java.util.Map;

/**
 * DTO object to retrieve the required fields from the call to External API's /latest endpoint, which returns
 * the latest Exchange Rates for a given Currency
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ExchangeRateDTO {
    private Map<String, Double> rates;

}
