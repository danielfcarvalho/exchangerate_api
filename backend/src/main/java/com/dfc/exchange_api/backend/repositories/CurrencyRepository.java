package com.dfc.exchange_api.backend.repositories;

import com.dfc.exchange_api.backend.models.Currency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CurrencyRepository extends JpaRepository<Currency, Long> {
}
