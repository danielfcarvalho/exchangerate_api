package com.dfc.exchange_api.backend.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * DTO object to retrieve the required fields from the call to External API's /symbols endpoint, which returns
 * the symbols supported by this API
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class FetchedSymbolsDTO {
    @JsonProperty("symbols")
    private TreeMap<String, CurrencyDTO> symbols;
}
