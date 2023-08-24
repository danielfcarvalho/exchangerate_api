package com.dfc.exchange_api.backend.models;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CurrencyDTO {
    private String description;
    private String code;
}
